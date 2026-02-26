package dalgrock.playlist.service;

import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.Emotion;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.Weekly;
import dalgrock.playlist.service.dto.WeeklyReportPayloadData;
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
import java.util.HashSet;
import java.util.Set;
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
     * 해당 사용자의 "이번 주" (오늘 기준 월~일 주) 리포트 병합용 페이로드 데이터를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Optional<WeeklyReportPayloadData> getReportPayloadData(Long userId) {
        LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
        int year = getYearOfWeek(today);
        int month = getMonthOfWeek(today);
        int week = getWeekOfMonth(today);
        return getReportPayloadData(userId, year, month, week);
    }

    /**
     * 지정한 year, month, week에 해당하는 주간 리포트 병합용 페이로드 데이터를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Optional<WeeklyReportPayloadData> getReportPayloadData(Long userId, int year, int month, int week) {
        Optional<Weekly> weeklyOpt = weeklyRepository.findByYearAndMonthAndWeek(year, month, week);
        if (weeklyOpt.isEmpty()) {
            return Optional.empty();
        }

        Weekly weekly = weeklyOpt.get();
        List<Record> records = recordRepository.findByUserIdAndWeeklyIdIn(userId, List.of(weekly.getId()));

        int[] prev = getPreviousWeek(year, month, week);
        List<Record> prevRecords = weeklyRepository.findByYearAndMonthAndWeek(prev[0], prev[1], prev[2])
                .map(w -> recordRepository.findByUserIdAndWeeklyIdIn(userId, List.of(w.getId())))
                .orElse(List.of());

        return Optional.of(buildPayloadDataFromRecords(weekly, records, prevRecords));
    }

    /**
     * 해당 사용자의 "이번 주" (오늘 기준 월~일 주) 기록으로 분석 데이터 문자열을 만듭니다.
     */
    @Transactional(readOnly = true)
    public String buildDataForAnalysis(Long userId) {
        LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
        int year = getYearOfWeek(today);
        int month = getMonthOfWeek(today);
        int week = getWeekOfMonth(today);
        return buildDataForAnalysis(userId, year, month, week);
    }

    /**
     * 지정한 year, month, week에 해당하는 주간 기록으로 분석 데이터 문자열을 만듭니다.
     */
    @Transactional(readOnly = true)
    public String buildDataForAnalysis(Long userId, int year, int month, int week) {
        Optional<Weekly> weeklyOpt = weeklyRepository.findByYearAndMonthAndWeek(year, month, week);
        if (weeklyOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 주차 데이터가 없습니다: year=%d, month=%d, week=%d".formatted(year, month, week));
        }

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

    /** 이전 주의 year, month, week 반환 */
    private static int[] getPreviousWeek(int year, int month, int week) {
        if (week > 1) {
            return new int[]{year, month, week - 1};
        }
        if (month > 1) {
            LocalDate lastDayOfPrevMonth = LocalDate.of(year, month, 1).minusDays(1);
            int prevMonth = lastDayOfPrevMonth.getMonthValue();
            int prevYear = lastDayOfPrevMonth.getYear();
            int prevWeek = getWeekOfMonth(lastDayOfPrevMonth);
            return new int[]{prevYear, prevMonth, prevWeek};
        }
        LocalDate lastDayOfPrevYear = LocalDate.of(year, 1, 1).minusDays(1);
        return new int[]{
                lastDayOfPrevYear.getYear(),
                lastDayOfPrevYear.getMonthValue(),
                getWeekOfMonth(lastDayOfPrevYear)
        };
    }

    private static LocalDate getMondayOfWeek(int year, int month, int week) {
        int dayOfMonth = (week - 1) * 7 + 1;
        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        return LocalDate.of(year, month, Math.min(dayOfMonth, maxDay));
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

    private WeeklyReportPayloadData buildPayloadDataFromRecords(Weekly weekly, List<Record> records, List<Record> prevRecords) {
        LocalDate monday = getMondayOfWeek(weekly.getYear(), weekly.getMonth(), weekly.getWeek());

        List<Map<String, String>> weeklyPlaylistMusics = new ArrayList<>();
        Map<String, List<String>> emotionToGenres = new LinkedHashMap<>();
        Map<String, List<String>> emotionToThumbnails = new LinkedHashMap<>();
        Map<String, List<String>> situationToThumbnails = new LinkedHashMap<>();
        List<String> allGenres = new ArrayList<>();
        List<Map<String, String>> allEmotionsForTop = new ArrayList<>();

        for (Record record : records) {
            List<Map<String, String>> emotionsWithCategory = record.getEmotions().stream()
                    .map(WeeklyReportDataService::toEmotionWithCategory)
                    .toList();
            allEmotionsForTop.addAll(emotionsWithCategory);

            List<String> situations = record.getSituationsToString();
            List<GetRecordMusicDto> musics = recordMusicRepository.findAllByRecordId(record.getId());

            for (GetRecordMusicDto m : musics) {
                String genre = (m.getGenre() != null && !m.getGenre().isBlank()) ? m.getGenre() : "미분류";
                allGenres.add(genre);
                String thumb = (m.getThumbnail() != null && !m.getThumbnail().isBlank()) ? m.getThumbnail() : "";

                weeklyPlaylistMusics.add(Map.of(
                        "title", m.getTitle() != null ? m.getTitle() : "",
                        "artist", m.getArtist() != null ? m.getArtist() : "",
                        "thumbnail", thumb));

                for (Map<String, String> em : emotionsWithCategory) {
                    String emotionVal = em.get("emotion");
                    emotionToGenres.computeIfAbsent(emotionVal, k -> new ArrayList<>()).add(genre);
                    if (!thumb.isBlank()) {
                        emotionToThumbnails.computeIfAbsent(emotionVal, k -> new ArrayList<>()).add(thumb);
                    }
                }
                for (String sit : situations) {
                    if (!thumb.isBlank()) {
                        situationToThumbnails.computeIfAbsent(sit, k -> new ArrayList<>()).add(thumb);
                    }
                }
            }
        }

        List<List<Map<String, String>>> dailyEmotionTagsByDay = buildDailyEmotionTagsByDay(records, monday);
        List<String> topEmotionTagsForSummary = buildTopEmotionTagsForSummary(allEmotionsForTop, 2);

        Map<String, Long> emotionCountsThisWeek = allEmotionsForTop.stream()
                .collect(Collectors.groupingBy(WeeklyReportDataService::emotionKey, Collectors.counting()));
        Map<String, Long> genreCountsThisWeek = allGenres.stream()
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        List<Map<String, String>> prevEmotions = prevRecords.stream()
                .flatMap(r -> r.getEmotions().stream().map(WeeklyReportDataService::toEmotionWithCategory))
                .toList();
        List<String> prevGenres = prevRecords.stream()
                .flatMap(r -> recordMusicRepository.findAllByRecordId(r.getId()).stream())
                .map(m -> (m.getGenre() != null && !m.getGenre().isBlank()) ? m.getGenre() : "미분류")
                .toList();
        Map<String, Long> emotionCountsPrevWeek = prevEmotions.stream()
                .collect(Collectors.groupingBy(WeeklyReportDataService::emotionKey, Collectors.counting()));
        Map<String, Long> genreCountsPrevWeek = prevGenres.stream()
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        List<Map<String, Object>> topEmotionGenreData = buildTopEmotionGenreData(emotionToGenres, emotionToThumbnails, emotionCountsThisWeek, 3);

        Set<String> seen = new HashSet<>();
        List<Map<String, String>> dedupedPlaylist = weeklyPlaylistMusics.stream()
                .filter(m -> seen.add(m.get("title") + "|" + m.get("artist")))
                .toList();

        String topEmotion = computeComparisonEmotion(emotionCountsThisWeek, emotionCountsPrevWeek);
        String topGenre = computeComparisonGenre(genreCountsThisWeek, genreCountsPrevWeek);

        Map<String, List<String>> sortedSituationThumbnails = situationToThumbnails.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        return new WeeklyReportPayloadData(
                weekly,
                dedupedPlaylist,
                dailyEmotionTagsByDay,
                topEmotionTagsForSummary,
                emotionToGenres,
                emotionToThumbnails,
                sortedSituationThumbnails,
                topEmotion,
                topGenre,
                emotionCountsThisWeek,
                genreCountsThisWeek,
                emotionCountsPrevWeek,
                genreCountsPrevWeek,
                topEmotionGenreData);
    }

    private List<List<Map<String, String>>> buildDailyEmotionTagsByDay(List<Record> records, LocalDate monday) {
        List<List<Map<String, String>>> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = monday.plusDays(i);
            List<Map<String, String>> dayEmotions = records.stream()
                    .filter(r -> r.getCreatedAt().toLocalDate().equals(dayDate))
                    .flatMap(r -> r.getEmotions().stream().map(WeeklyReportDataService::toEmotionWithCategory))
                    .filter(m -> !"미상".equals(m.get("emotion")))
                    .map(m -> Map.of("category", m.get("category"), "value", m.get("emotion")))
                    .distinct()
                    .toList();
            result.add(dayEmotions);
        }
        return result;
    }

    private List<String> buildTopEmotionTagsForSummary(List<Map<String, String>> allEmotions, int limit) {
        return allEmotions.stream()
                .collect(Collectors.groupingBy(WeeklyReportDataService::emotionKey, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> !e.getKey().endsWith("|미상"))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> e.getKey().split("\\|", 2)[1])
                .toList();
    }

    private List<Map<String, Object>> buildTopEmotionGenreData(
            Map<String, List<String>> emotionToGenres,
            Map<String, List<String>> emotionToThumbnails,
            Map<String, Long> emotionCountsThisWeek,
            int limit) {
        return emotionCountsThisWeek.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit * 2)
                .filter(e -> !e.getKey().endsWith("|미상"))
                .map(e -> {
                    String[] parts = e.getKey().split("\\|", 2);
                    String emotion = parts[1];
                    List<String> genres = emotionToGenres.getOrDefault(emotion, List.of());
                    List<String> thumbs = emotionToThumbnails.getOrDefault(emotion, List.of());
                    if (genres.isEmpty() && thumbs.isEmpty()) {
                        return null;
                    }
                    return Map.<String, Object>of(
                            "emotion", emotion,
                            "genres", dedupeList(genres),
                            "thumbnail", dedupeList(thumbs));
                })
                .filter( m -> m != null)
                .limit(limit)
                .toList();
    }

    private static List<String> dedupeList(List<String> list) {
        return list.stream().filter(s -> s != null && !s.isBlank()).distinct().toList();
    }

    private String computeComparisonEmotion(Map<String, Long> thisWeek, Map<String, Long> prevWeek) {
        if (prevWeek.isEmpty()) {
            return thisWeek.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().split("\\|", 2)[1])
                    .orElse("");
        }
        return thisWeek.entrySet().stream()
                .map(e -> {
                    long diff = e.getValue() - prevWeek.getOrDefault(e.getKey(), 0L);
                    return Map.entry(e.getKey(), diff);
                })
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().split("\\|", 2)[1])
                .orElseGet(() -> thisWeek.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(x -> x.getKey().split("\\|", 2)[1])
                        .orElse(""));
    }

    private String computeComparisonGenre(Map<String, Long> thisWeek, Map<String, Long> prevWeek) {
        if (prevWeek.isEmpty()) {
            return thisWeek.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
        }
        return thisWeek.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue() - prevWeek.getOrDefault(e.getKey(), 0L)))
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> thisWeek.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(""));
    }

}
