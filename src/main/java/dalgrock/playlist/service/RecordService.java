package dalgrock.playlist.service;

import dalgrock.playlist.core.exception.ErrorCode;
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
import dalgrock.playlist.service.dto.response.GetRecordMusicResponse;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public CreateRecordResponse createRecord(Long userId, CreateRecordCommand command) {
        LocalDate today = LocalDate.now();
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
        String artist = command.artist() != null ? command.artist() : "";
        String title = command.title() != null ? command.title() : "";
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
