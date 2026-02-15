package dalgrock.playlist.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /v1/weekly 응답.
 * year, month 기준 해당 달의 주차별 레코드 목록.
 */
public record GetWeeklyResponse(
        int year,
        int month,
        List<WeeklyItem> weekly
) {

    public record WeeklyItem(
            int week,
            List<RecordItem> records
    ) {}

    public record RecordItem(
            Long recordId,
            LocalDateTime createdAt,
            String thumbnail
    ) {}
}
