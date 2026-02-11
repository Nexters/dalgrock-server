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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    /**
     * 오늘·7일 전·14일 전 주차의 (year, month, week)로 해당 userId의 records를 조회한 뒤,
     * 이번주 → 7일 전 주 → 14일 전 주 순으로 응답을 가공하여 반환.
     */
    public GetRecordResponse getRecords(Long userId) {
        LocalDate today = LocalDate.now();
        List<LocalDate> weekDates = List.of(today, today.minusDays(7), today.minusDays(14));

        List<WeekContext> weekContexts = weekDates.stream()
                .map(d -> {
                    int y = getYearOfWeek(d);
                    int m = getMonthOfWeek(d);
                    int w = getWeekOfMonth(d);
                    return new WeekContext(y, m, w, weeklyRepository.findByYearAndMonthAndWeek(y, m, w));
                })
                .toList();

        List<Long> weeklyIds = weekContexts.stream()
                .flatMap(wc -> wc.weeklyOpt().stream())
                .map(Weekly::getId)
                .toList();

        List<Record> records = weeklyIds.isEmpty()
                ? List.of()
                : recordRepository.findByUserIdAndWeeklyIdIn(userId, weeklyIds);

        GetRecordResponse.TodayRecordItem todayItem = records.stream()
                .filter(r -> r.getCreatedAt().toLocalDate().equals(today))
                .findFirst()
                .map(r -> new GetRecordResponse.TodayRecordItem(r.getId(), nullToEmpty(r.getThumbnail())))
                .orElse(null);

        List<GetRecordResponse.WeeklyGroupItem> weekly = weekContexts.stream()
                .map(wc -> buildWeeklyGroupItem(wc, records))
                .toList();

        return new GetRecordResponse(todayItem, weekly);
    }

    private GetRecordResponse.WeeklyGroupItem buildWeeklyGroupItem(WeekContext wc, List<Record> records) {
        String title = wc.weeklyOpt()
                .map(w -> w.getTitle() != null ? w.getTitle() : formatWeeklyTitle(wc.month(), wc.week()))
                .orElse(formatWeeklyTitle(wc.month(), wc.week()));
        List<GetRecordResponse.WeeklyRecordItem> items = records.stream()
                .filter(r -> sameWeek(r, wc.year(), wc.month(), wc.week()))
                .sorted(Comparator.comparing(Record::getCreatedAt).reversed())
                .map(r -> new GetRecordResponse.WeeklyRecordItem(r.getId(), nullToEmpty(r.getThumbnail()), r.getCreatedAt()))
                .toList();
        return new GetRecordResponse.WeeklyGroupItem(title, wc.year(), wc.month(), wc.week(), items);
    }

    private record WeekContext(int year, int month, int week, Optional<Weekly> weeklyOpt) {}

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static String formatWeeklyTitle(int month, int week) {
        return  month + "월 " + week + "주차";
    }

    private static boolean sameWeek(Record r, int year, int month, int week) {
        Weekly w = r.getWeekly();
        return w != null && w.getYear() == year && w.getMonth() == month && w.getWeek() == week;
    }

    @Transactional
    public CreateRecordResponse createRecord(Long userId, CreateRecordCommand command) {
        LocalDate today = LocalDate.now();
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
     * 오늘 날짜가 속한 주의 월요일 기준으로 (year, month, week) 계산.
     */
    private int getYearOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY).getYear();
    }

    private int getMonthOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY).getMonthValue();
    }

    private int getWeekOfMonth(LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
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
