package dalgrock.playlist.service;

import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.Weekly;
import dalgrock.playlist.service.dto.response.GetWeeklyResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WeeklyService {

    private final WeeklyRepository weeklyRepository;
    private final RecordRepository recordRepository;

    /**
     * year, month에 해당하는 주차별 레코드를 userId 기준으로 조회.
     */
    public GetWeeklyResponse getWeeklyRecords(Long userId, int year, int month) {
        List<Weekly> weeklies = weeklyRepository.findByYearAndMonth(year, month);
        if (weeklies.isEmpty()) {
            return new GetWeeklyResponse(year, month, List.of());
        }

        List<Long> weeklyIds = weeklies.stream().map(Weekly::getId).toList();
        List<Record> allRecords = recordRepository.findByUserIdAndWeeklyIdIn(userId, weeklyIds);
        Map<Long, List<Record>> recordsByWeeklyId = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getWeekly().getId()));

        List<GetWeeklyResponse.WeeklyItem> weeklyItems = new ArrayList<>();
        for (Weekly w : weeklies) {
            List<Record> records = recordsByWeeklyId.getOrDefault(w.getId(), List.of());
            List<GetWeeklyResponse.RecordItem> recordItems = records.stream()
                    .sorted(Comparator.comparing(Record::getCreatedAt))
                    .map(record -> toRecordItem(record))
                    .toList();
            weeklyItems.add(new GetWeeklyResponse.WeeklyItem(w.getWeek(), recordItems));
        }

        return new GetWeeklyResponse(year, month, weeklyItems);
    }

    private GetWeeklyResponse.RecordItem toRecordItem(Record record) {
        String thumbnail = record.getThumbnail() != null ? record.getThumbnail() : "";
        return new GetWeeklyResponse.RecordItem(
                record.getId(),
                record.getCreatedAt(),
                thumbnail
        );
    }
}
