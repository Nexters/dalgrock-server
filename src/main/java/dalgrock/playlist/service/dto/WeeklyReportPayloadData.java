package dalgrock.playlist.service.dto;

import dalgrock.playlist.model.Weekly;
import java.util.List;
import java.util.Map;

/**
 * 주간 리포트 병합에 필요한 Record 기반 데이터.
 */
public record WeeklyReportPayloadData(
        Weekly weekly,
        List<Map<String, String>> weeklyPlaylistMusics,
        /** 요일별(월~일 7개) 감정 태그. 기록 없는 날은 빈 리스트 */
        List<List<Map<String, String>>> dailyEmotionTagsByDay,
        /** summaryTags용 상위 2개 감정 value (예: ["위로", "전환"]) */
        List<String> topEmotionTagsForSummary,
        Map<String, List<String>> emotionToGenres,
        Map<String, List<String>> emotionToThumbnails,
        /** 상황별 썸네일 (썸네일 개수 내림차순 정렬용) */
        Map<String, List<String>> situationToThumbnails,
        String topEmotion,
        String topGenre,
        /** 이번 주 감정별 카운트 (emotion display value -> count) */
        Map<String, Long> emotionCountsThisWeek,
        /** 이번 주 장르별 카운트 */
        Map<String, Long> genreCountsThisWeek,
        /** 지난 주 감정별 카운트 (없으면 empty) */
        Map<String, Long> emotionCountsPrevWeek,
        /** 지난 주 장르별 카운트 (없으면 empty) */
        Map<String, Long> genreCountsPrevWeek,
        /** LLM emotionGenreDescriptions용: 감정별 {genres, thumbnails}가 있는 상위 3개 감정 */
        List<Map<String, Object>> topEmotionGenreData
) {
}
