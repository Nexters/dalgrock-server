package dalgrock.playlist.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /v1/records 응답.
 * - today: 오늘 기록 1건 (없으면 null)
 * - weekly: 주차별 그룹 배열 (각 주차에 records 배열)
 */
public record GetRecordResponse(
        TodayRecordItem today,
        List<WeeklyGroupItem> weekly
) {

    /** 오늘 기록 1건 (recordId, thumbnail) */
    public record TodayRecordItem(
            Long recordId,
            String thumbnail
    ) {}

    /** 주차 한 덩어리 (title, year, month, week, records) */
    public record WeeklyGroupItem(
            String title,
            int year,
            int month,
            int week,
            List<WeeklyRecordItem> records
    ) {}

    /** 주차 내 기록 한 건 (recordId, thumbnail, createdAt) */
    public record WeeklyRecordItem(
            Long recordId,
            String thumbnail,
            LocalDateTime createdAt
    ) {}
}
