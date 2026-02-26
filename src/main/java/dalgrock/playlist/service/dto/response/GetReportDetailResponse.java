package dalgrock.playlist.service.dto.response;

import java.util.List;
import java.util.Map;

public record GetReportDetailResponse(
        OverallSummary overallSummary,
        WeeklyPlaylist weeklyPlaylist,
        WeeklyEmotionSummary weeklyEmotionSummary,
        List<EmotionGenreDescription> emotionGenreDescriptions,
        List<ContextSummary> contextSummaries,
        WeeklyComparison weeklyComparison
) {

    public record OverallSummary(String title, List<String> summaryTags) {}

    public record WeeklyPlaylist(List<Music> musics) {
        public record Music(String title, String artist, String thumbnail) {}
    }

    public record WeeklyEmotionSummary(String title, Map<String, List<String>> emotions) {}

    public record EmotionGenreDescription(
            String emotion,
            String description,
            List<String> genres,
            List<String> thumbnail
    ) {}

    public record ContextSummary(String value, List<String> thumbnail) {}

    public record WeeklyComparison(String emotion, String genre, String nextWeekSuggestion) {}
}
