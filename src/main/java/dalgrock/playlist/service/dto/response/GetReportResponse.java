package dalgrock.playlist.service.dto.response;

import java.util.List;

public record GetReportResponse(
        int year,
        int month,
        List<WeeklyReportItem> weekly
) {

    public record WeeklyReportItem(
            int week,
            Long reportId,
            String status,
            String title,
            int recordCount,
            List<String> emotions,
            String representativeThumbnail,
            List<String> thumbnails
    ) {}
}
