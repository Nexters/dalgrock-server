package dalgrock.playlist.service;

import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.Emotion;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.Weekly;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주간 리포트용 분석 데이터 문자열을 생성합니다.
 * DeepSeek 프롬프트의 "이번 주 기록 데이터" 블록과 동일한 형식으로 맞춥니다.
 */
@RequiredArgsConstructor
@Service
public class WeeklyReportDataService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    private final WeeklyRepository weeklyRepository;
    private final RecordRepository recordRepository;
    private final RecordMusicRepository recordMusicRepository;

    /**
     * 해당 사용자의 "이번 주" (오늘 기준 월~일 주) 기록으로 분석 데이터 문자열을 만듭니다.
     */
    @Transactional(readOnly = true)
    public String buildDataForAnalysis(Long userId) {
        LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
        int year = getYearOfWeek(today);
        int month = getMonthOfWeek(today);
        int week = getWeekOfMonth(today);

        Optional<Weekly> weeklyOpt = weeklyRepository.findByYearAndMonthAndWeek(year, month, week);

        List<Record> records = recordRepository.findByUserIdAndWeeklyIdIn(userId, List.of(weeklyOpt.get().getId()));
        return buildDataForAnalysisFromRecords(records);
    }

    private static int getYearOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getYear();
    }

    private static int getMonthOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getMonthValue();
    }

    private static int getWeekOfMonth(LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return (monday.getDayOfMonth() - 1) / 7 + 1;
    }

    private static Map<String, String> toEmotionWithCategory(Emotion emotion) {
        String category = emotion.getValue().getCategory().getValue();
        String emotionValue = emotion.getDisplayValue();
        return Map.of("category", category, "emotion", emotionValue);
    }

    private static String emotionKey(Map<String, String> em) {
        return em.get("category") + "|" + em.get("emotion");
    }

    private String buildDataForAnalysisFromRecords(List<Record> records) {
        int totalRecords = records.size();
        List<Map<String, String>> allEmotions = new ArrayList<>();
        List<String> allGenres = new ArrayList<>();
        List<List<Map<String, String>>> dailyEmotions = new ArrayList<>();
        Map<String, List<Map<String, Object>>> emotionGenreCombinations = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> situationMusics = new LinkedHashMap<>();
        int totalMusicCount = 0;

        for (Record record : records) {
            List<Map<String, String>> emotionsWithCategory = record.getEmotions().stream()
                    .map(WeeklyReportDataService::toEmotionWithCategory)
                    .toList();
            allEmotions.addAll(emotionsWithCategory);
            dailyEmotions.add(emotionsWithCategory.isEmpty() ? List.of() : emotionsWithCategory);

            List<String> situations = record.getSituationsToString();
            List<GetRecordMusicDto> musics = recordMusicRepository.findAllByRecordId(record.getId());
            totalMusicCount += musics.size();

            for (GetRecordMusicDto m : musics) {
                String genre = (m.getGenre() != null && !m.getGenre().isBlank()) ? m.getGenre() : "미분류";
                allGenres.add(genre);
                for (Map<String, String> em : emotionsWithCategory) {
                    String key = emotionKey(em);
                    emotionGenreCombinations
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .add(Map.<String, Object>of(genre, 1));
                }
                for (String sit : situations) {
                    situationMusics
                            .computeIfAbsent(sit, k -> new ArrayList<>())
                            .add(Map.of("title", m.getTitle() != null ? m.getTitle() : "", "genre", genre));
                }
            }
        }

        long uniqueMusicCount = records.stream()
                .flatMap(r -> recordMusicRepository.findAllByRecordId(r.getId()).stream())
                .map(m -> m.getTitle() + "|" + m.getArtist())
                .distinct()
                .count();

        Map<String, Long> emotionCounts = allEmotions.stream()
                .collect(Collectors.groupingBy(WeeklyReportDataService::emotionKey, Collectors.counting()));
        Map<String, Long> genreCounts = allGenres.stream()
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        List<Map<String, String>> topEmotions = emotionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> {
                    String[] parts = e.getKey().split("\\|", 2);
                    return Map.<String, String>of("category", parts[0], "emotion", parts[1]);
                })
                .toList();
        List<String> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 감정별 장르 조합 단순화: {category, emotion} -> [ { "장르": count }, ... ]
        Map<String, Map<String, Long>> emotionGenreAggregated = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : emotionGenreCombinations.entrySet()) {
            Map<String, Long> genreCount = new LinkedHashMap<>();
            for (Map<String, Object> pair : e.getValue()) {
                for (Map.Entry<String, Object> entry : pair.entrySet()) {
                    String g = entry.getKey();
                    genreCount.merge(g, 1L, Long::sum);
                }
            }
            emotionGenreAggregated.put(e.getKey(), genreCount);
        }

        List<Map<String, Object>> emotionCountsFormatted = emotionCounts.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("\\|", 2);
                    return Map.<String, Object>of(
                            "category", parts[0],
                            "emotion", parts[1],
                            "count", e.getValue()
                    );
                })
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("  ## 이번 주 기록 데이터 (사용자 데이터)\n");
        sb.append("  ### 전체 통계\n");
        sb.append("  {\"총 기록 수\": ").append(totalRecords)
                .append(", \"총 음악 수\": ").append(totalMusicCount)
                .append(", \"고유 음악 수\": ").append(uniqueMusicCount).append("}\n\n");
        sb.append("  ### 감정 분포 (category, emotion, count)\n  ").append(emotionCountsFormatted).append("\n\n");
        sb.append("  ### 음악 장르 분포\n  ").append(genreCounts.toString()).append("\n\n");
        sb.append("  ### 주요 감정 (상위 3개, {category, emotion})\n  ").append(topEmotions).append("\n\n");
        sb.append("  ### 주요 장르 (상위 3개)\n  ").append(topGenres).append("\n\n");
        sb.append("  ### 일별 감정 변화 ({category, emotion}[][])\n\n  ").append(dailyEmotions).append("\n\n");
        List<Map<String, Object>> emotionGenreFormatted = emotionGenreAggregated.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("\\|", 2);
                    return Map.<String, Object>of(
                            "category", parts[0],
                            "emotion", parts[1],
                            "genres", e.getValue()
                    );
                })
                .toList();
        sb.append("  ### 감정별 음악 장르 조합 ({category, emotion, genres})\n  ").append(emotionGenreFormatted).append("\n\n");
        sb.append("  ### 상황별 음악 청취\n  ").append(situationMusics).append("\n\n");
        sb.append("  ### 지난 주와의 비교\n\n  ### 감정 변화\n  {\"증가한 감정\": [], \"감소한 감정\": []}\n\n");
        sb.append("  ### 장르 변화\n  {\"증가한 장르\": [], \"감소한 장르\": []}\n\n");
        sb.append("  ### 지난 주 통계\n  {\"감정 분포\": {}, \"장르 분포\": {}}\n");
        return sb.toString();
    }
}
