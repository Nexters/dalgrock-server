package dalgrock.playlist.service;

import dalgrock.playlist.core.exception.ErrorCode;
import dalgrock.playlist.core.exception.RecordAlreadyExistsTodayException;
import dalgrock.playlist.core.exception.RecordNotFoundException;
import dalgrock.playlist.core.exception.UnauthorizedException;
import dalgrock.playlist.infrastructure.repository.MusicRepository;
import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.Emotion;
import dalgrock.playlist.model.Music;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.RecordMusic;
import dalgrock.playlist.model.Situation;
import dalgrock.playlist.model.Weekly;
import dalgrock.playlist.service.dto.command.CreateRecordCommand;
import dalgrock.playlist.service.dto.command.CreateRecordMusicCommand;
import dalgrock.playlist.service.dto.response.CreateRecordResponse;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import dalgrock.playlist.service.dto.response.GetRecordMusicResponse;
import dalgrock.playlist.service.dto.response.GetRecordResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordMusicRepository recordMusicRepository;
    private final MusicRepository musicRepository;
    private final WeeklyRepository weeklyRepository;

    public GetRecordDetailResponse getRecordDetail(Long userId, Long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND.getMessage()));

        if (!record.getUserId().equals(userId)) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN.getMessage());
        }

        List<GetRecordMusicDto> recordMusics = recordMusicRepository.findAllByRecordId(record.getId());

        List<GetRecordMusicResponse> recordMusicResponses = recordMusics.stream()
                .map(GetRecordMusicResponse::from)
                .toList();

        return GetRecordDetailResponse.of(record, recordMusicResponses);
    }

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    private static final int DAYS_PER_WEEK = 7;

    /**
     * 이번 주(월요일~일요일)에 해당하는 userId의 records를 조회하여
     * recordId, createdAt, musics, emotions, isToday 형태로 반환.
     * - records는 항상 7개 (월~일 순). 기록 없는 날은 recordId/createdAt null, musics/emotions [], isToday만 해당 날짜 여부.
     * 기준 시간대는 Asia/Seoul.
     */
    public GetRecordResponse getRecords(Long userId) {
        LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
        LocalDate weekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekSunday = weekMonday.plusDays(6);

        int year = getYearOfWeek(today);
        int month = getMonthOfWeek(today);
        int week = getWeekOfMonth(today);

        Optional<Weekly> weeklyOpt = weeklyRepository.findByYearAndMonthAndWeek(year, month, week);
        List<Record> records = weeklyOpt
                .map(w -> recordRepository.findByUserIdAndWeeklyIdIn(userId, List.of(w.getId())))
                .orElse(List.of());

        Map<LocalDate, Record> recordByDate = new HashMap<>();
        for (Record r : records) {
            if (isDateInRange(r.getCreatedAt(), weekMonday, weekSunday)) {
                LocalDate d = r.getCreatedAt().atZone(APP_ZONE).toLocalDate();
                recordByDate.put(d, r);
            }
        }

        List<GetRecordResponse.RecordItem> recordItems = new ArrayList<>(DAYS_PER_WEEK);
        for (int i = 0; i < DAYS_PER_WEEK; i++) {
            LocalDate day = weekMonday.plusDays(i);
            Record record = recordByDate.get(day);
            boolean isToday = day.equals(today);
            recordItems.add(record != null
                    ? toRecordItem(record, isToday)
                    : emptyRecordItem(isToday));
        }

        return new GetRecordResponse(recordItems);
    }

    private static GetRecordResponse.RecordItem emptyRecordItem(boolean isToday) {
        return new GetRecordResponse.RecordItem(null, null, List.of(), List.of(), isToday);
    }

    /** createdAt의 날짜가 [weekMonday, weekSunday] 안에 있는지 검사. 저장이 KST면 atZone(APP_ZONE), UTC면 UTC→Seoul 변환 필요. */
    private static boolean isDateInRange(LocalDateTime createdAt, LocalDate weekMonday, LocalDate weekSunday) {
        LocalDate d = createdAt.atZone(APP_ZONE).toLocalDate();
        return !d.isBefore(weekMonday) && !d.isAfter(weekSunday);
    }

    private GetRecordResponse.RecordItem toRecordItem(Record record, boolean isToday) {
        List<GetRecordMusicDto> recordMusics = recordMusicRepository.findAllByRecordId(record.getId());
        List<GetRecordResponse.MusicThumbnailItem> musics = recordMusics.stream()
                .map(dto -> new GetRecordResponse.MusicThumbnailItem(nullToEmpty(dto.getThumbnail())))
                .toList();
        List<String> emotions = record.getEmotionsToString();
        return new GetRecordResponse.RecordItem(
                record.getId(),
                record.getCreatedAt(),
                musics,
                emotions,
                isToday
        );
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    @Transactional
    public CreateRecordResponse createRecord(Long userId, CreateRecordCommand command) {
        LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        if (recordRepository.existsByUserIdAndCreatedAtBetween(userId, startOfDay, startOfNextDay)) {
            throw new RecordAlreadyExistsTodayException();
        }

        int year = getYearOfWeek(today);
        int month = getMonthOfWeek(today);
        int week = getWeekOfMonth(today);

        Weekly weekly = findOrCreateWeekly(year, month, week);

        List<Emotion> emotions = new ArrayList<>(toEmotions(command.emotions()));
        List<Situation> situations = new ArrayList<>(toSituations(command.situations()));

        String thumbnail = command.musics() != null && !command.musics().isEmpty()
                ? command.musics().get(0).thumbnail()
                : "";

        Record record = Record.builder()
                .userId(userId)
                .thumbnail(thumbnail != null ? thumbnail : "")
                .location(command.location())
                .content(command.content())
                .emotions(emotions)
                .situations(situations)
                .weekly(weekly)
                .build();

        Record savedRecord = recordRepository.save(record);

        if (command.musics() != null) {
            for (CreateRecordMusicCommand musicCommand : command.musics()) {
                Music music = findOrCreateMusic(musicCommand);
                RecordMusic recordMusic = RecordMusic.builder()
                        .recordId(savedRecord.getId())
                        .musicId(music.getId())
                        .build();
                recordMusicRepository.save(recordMusic);
            }
        }

        return CreateRecordResponse.from(savedRecord);
    }

    private List<Emotion> toEmotions(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> new Emotion("", value))
                .toList();
    }

    private List<Situation> toSituations(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> new Situation("", value))
                .toList();
    }

    private Music findOrCreateMusic(CreateRecordMusicCommand command) {
        String artist = command.artist();
        String title = command.title();
        return musicRepository.findByArtistAndTitle(artist, title)
                .orElseGet(() -> {
                    Music newMusic = Music.builder()
                            .artist(artist)
                            .title(title)
                            .thumbnail(command.thumbnail() != null ? command.thumbnail() : "")
                            .genre(null)
                            .build();
                    return musicRepository.save(newMusic);
                });
    }

    /**
     * 주차는 월요일 시작 ~ 일요일 끝.
     * previousOrSame(MONDAY)로 주의 첫날(월요일)을 명시적으로 구해 (year, month, week) 계산.
     */
    private int getYearOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getYear();
    }

    private int getMonthOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getMonthValue();
    }

    private int getWeekOfMonth(LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return (monday.getDayOfMonth() - 1) / 7 + 1;
    }

    private Weekly findOrCreateWeekly(int year, int month, int week) {
        return weeklyRepository.findByYearAndMonthAndWeek(year, month, week)
                .orElseGet(() -> {
                    Weekly newWeekly = Weekly.builder()
                            .year(year)
                            .month(month)
                            .week(week)
                            .build();
                    return weeklyRepository.save(newWeekly);
                });
    }
}
