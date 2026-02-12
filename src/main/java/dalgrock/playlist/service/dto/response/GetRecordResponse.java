package dalgrock.playlist.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /v1/records 응답.
 * - records: 이번 주 기록 배열 (recordId, createdAt, musics, emotions)
 */
public record GetRecordResponse(
        List<RecordItem> records
) {

    /** 기록 한 건 (recordId, createdAt, musics, emotions) */
    public record RecordItem(
            Long recordId,
            LocalDateTime createdAt,
            List<MusicThumbnailItem> musics,
            List<String> emotions
    ) {}

    /** 음악 썸네일 (thumbnail) */
    public record MusicThumbnailItem(
            String thumbnail
    ) {}
}
