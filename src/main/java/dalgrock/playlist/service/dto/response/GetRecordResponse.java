package dalgrock.playlist.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /v1/records 응답.
 * - records: 항상 7개 (월~일 순, 늦은 날짜가 뒤). 기록 없는 날은 recordId/createdAt null, musics/emotions 빈 배열, isToday만 해당 날짜 여부.
 */
public record GetRecordResponse(
        List<RecordItem> records
) {

    /** 기록 한 건 또는 빈 슬롯 (기록 없으면 recordId·createdAt null, musics·emotions 빈 배열, isToday로 오늘 여부 표시) */
    public record RecordItem(
            Long recordId,
            LocalDateTime createdAt,
            List<MusicThumbnailItem> musics,
            List<String> emotions,
            boolean isToday
    ) {}

    /** 음악 썸네일 (thumbnail) */
    public record MusicThumbnailItem(
            String thumbnail
    ) {}
}
