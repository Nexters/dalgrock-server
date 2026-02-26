package dalgrock.playlist.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dalgrock.playlist.core.exception.BusinessException;
import dalgrock.playlist.core.exception.ErrorCode;
import dalgrock.playlist.core.exception.ReportNotFoundException;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.ReportRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.Report;
import dalgrock.playlist.model.ReportStatus;
import dalgrock.playlist.model.Weekly;
import dalgrock.playlist.service.dto.response.GetReportDetailResponse;
import dalgrock.playlist.service.dto.response.GetReportResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    private static final List<String> DAY_ORDER = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_LABEL = Map.of(
            DayOfWeek.MONDAY, "Mon",
            DayOfWeek.TUESDAY, "Tue",
            DayOfWeek.WEDNESDAY, "Wed",
            DayOfWeek.THURSDAY, "Thu",
            DayOfWeek.FRIDAY, "Fri",
            DayOfWeek.SATURDAY, "Sat",
            DayOfWeek.SUNDAY, "Sun"
    );

    private final WeeklyRepository weeklyRepository;
    private final RecordRepository recordRepository;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public GetReportResponse getMonthlyReport(Long userId, int year, int month) {
        List<Weekly> weeklies = weeklyRepository.findByYearAndMonth(year, month);
        if (weeklies.isEmpty()) {
            return new GetReportResponse(year, month, List.of());
        }

        List<Long> weeklyIds = weeklies.stream().map(Weekly::getId).toList();

        List<Record> allRecords = recordRepository.findByUserIdAndWeeklyIdIn(userId, weeklyIds);
        Map<Long, List<Record>> recordsByWeeklyId = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getWeekly().getId()));

        List<Report> allReports = reportRepository.findByUserIdAndWeeklyIdIn(userId, weeklyIds);
        Map<Long, Report> reportByWeeklyId = allReports.stream()
                .collect(Collectors.toMap(r -> r.getWeekly().getId(), r -> r, (a, b) -> a));

        LocalDate currentWeekMonday = LocalDate.now(APP_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<GetReportResponse.WeeklyReportItem> items = weeklies.stream()
                .sorted(Comparator.comparingInt(Weekly::getWeek).reversed())
                .flatMap(weekly -> buildWeeklyItem(
                        weekly,
                        recordsByWeeklyId.getOrDefault(weekly.getId(), List.of()),
                        reportByWeeklyId.get(weekly.getId()),
                        currentWeekMonday
                ).stream())
                .toList();

        return new GetReportResponse(year, month, items);
    }

    private Optional<GetReportResponse.WeeklyReportItem> buildWeeklyItem(
            Weekly weekly, List<Record> records, Report report, LocalDate currentWeekMonday) {

        if (report != null && report.getStatus() == ReportStatus.COMPLETED) {
            int recordCount = records.size();
            List<String> allThumbnails = collectThumbnails(records);
            String representativeThumbnail = allThumbnails.isEmpty() ? null : allThumbnails.get(0);
            String title = extractTitle(report.getContent());
            List<String> emotions = extractSummaryTags(report.getContent());
            return Optional.of(new GetReportResponse.WeeklyReportItem(
                    weekly.getWeek(), report.getId(), "COMPLETED",
                    title, recordCount, emotions,
                    representativeThumbnail, buildPaddedThumbnails(allThumbnails, representativeThumbnail)
            ));
        }

        if (records.isEmpty()) {
            return Optional.empty();
        }

        LocalDate weeklyMonday = getWeeklyMonday(weekly);
        if (weeklyMonday.isBefore(currentWeekMonday)) {
            return Optional.empty();
        }

        int recordCount = records.size();
        List<String> allThumbnails = collectThumbnails(records);
        String representativeThumbnail = allThumbnails.isEmpty() ? null : allThumbnails.get(0);
        List<String> emotions = extractEmotionsFromRecords(records);
        Long reportId = report != null ? report.getId() : null;
        return Optional.of(new GetReportResponse.WeeklyReportItem(
                weekly.getWeek(), reportId, "ANALYZING",
                null, recordCount, emotions,
                representativeThumbnail, buildPaddedThumbnails(allThumbnails, representativeThumbnail)
        ));
    }

    private List<String> collectThumbnails(List<Record> records) {
        return records.stream()
                .map(Record::getThumbnail)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .limit(5)
                .toList();
    }

    /**
     * Weekly(year, month, week)에 해당하는 월요일을 반환합니다.
     * week 번호는 해당 월요일의 dayOfMonth 기준: (dayOfMonth - 1) / 7 + 1
     */
    private LocalDate getWeeklyMonday(Weekly weekly) {
        LocalDate firstDayOfBucket = LocalDate.of(weekly.getYear(), weekly.getMonth(), (weekly.getWeek() - 1) * 7 + 1);
        return firstDayOfBucket.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /**
     * representativeThumbnail 1개 + 나머지 썸네일로 thumbnails 배열 4개를 구성.
     * 부족 시 representativeThumbnail로 패딩.
     */
    private List<String> buildPaddedThumbnails(List<String> allThumbnails, String representativeThumbnail) {
        if (representativeThumbnail == null) {
            return new ArrayList<>();
        }
        List<String> result = allThumbnails.size() > 1
                ? new ArrayList<>(allThumbnails.subList(1, allThumbnails.size()))
                : new ArrayList<>();
        while (result.size() < 4) {
            result.add(representativeThumbnail);
        }
        return result;
    }

    private List<String> extractEmotionsFromRecords(List<Record> records) {
        return records.stream()
                .flatMap(r -> r.getEmotions().stream())
                .map(e -> e.getDisplayValue())
                .distinct()
                .toList();
    }

    private String extractTitle(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode titleNode = root.path("overallSummary").path("title");
            return titleNode.isMissingNode() ? null : titleNode.asText(null);
        } catch (Exception e) {
            log.warn("리포트 content에서 title 파싱 실패", e);
            return null;
        }
    }

    private List<String> extractSummaryTags(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode tagsNode = root.path("overallSummary").path("summaryTags");
            if (tagsNode.isMissingNode() || !tagsNode.isArray()) {
                return List.of();
            }
            List<String> tags = new ArrayList<>();
            tagsNode.forEach(n -> tags.add(n.asText()));
            return tags;
        } catch (Exception e) {
            log.warn("리포트 content에서 summaryTags 파싱 실패", e);
            return List.of();
        }
    }

    public GetReportDetailResponse getReportDetail(Long userId, Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String content = report.getContent();

        GetReportDetailResponse.OverallSummary overallSummary =
                parseSection(content, "overallSummary", GetReportDetailResponse.OverallSummary.class);
        GetReportDetailResponse.WeeklyPlaylist weeklyPlaylist =
                parseSection(content, "weeklyPlaylist", GetReportDetailResponse.WeeklyPlaylist.class);
        String weeklyEmotionTitle = extractWeeklyEmotionTitle(content);
        List<GetReportDetailResponse.EmotionGenreDescription> emotionGenreDescriptions =
                parseSectionAsList(content, "emotionGenreDescriptions", GetReportDetailResponse.EmotionGenreDescription.class);
        List<GetReportDetailResponse.ContextSummary> contextSummaries =
                parseSectionAsList(content, "contextSummaries", GetReportDetailResponse.ContextSummary.class);
        GetReportDetailResponse.WeeklyComparison weeklyComparison =
                parseSection(content, "weeklyComparison", GetReportDetailResponse.WeeklyComparison.class);

        Long weeklyId = report.getWeekly().getId();
        List<Record> records = recordRepository.findByUserIdAndWeeklyIdIn(userId, List.of(weeklyId));
        Map<String, List<String>> emotionsMap = buildWeeklyEmotionsMap(records);

        GetReportDetailResponse.WeeklyEmotionSummary weeklyEmotionSummary =
                new GetReportDetailResponse.WeeklyEmotionSummary(weeklyEmotionTitle, emotionsMap);

        return new GetReportDetailResponse(
                overallSummary,
                weeklyPlaylist,
                weeklyEmotionSummary,
                emotionGenreDescriptions,
                contextSummaries,
                weeklyComparison
        );
    }

    private <T> T parseSection(String content, String key, Class<T> type) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode node = root.path(key);
            if (node.isMissingNode() || node.isNull()) {
                return null;
            }
            return objectMapper.treeToValue(node, type);
        } catch (Exception e) {
            log.warn("리포트 content에서 {} 파싱 실패", key, e);
            return null;
        }
    }

    private <T> List<T> parseSectionAsList(String content, String key, Class<T> elementType) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode node = root.path(key);
            if (node.isMissingNode() || node.isNull() || !node.isArray()) {
                return List.of();
            }
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.convertValue(node, listType);
        } catch (Exception e) {
            log.warn("리포트 content에서 {} 파싱 실패", key, e);
            return List.of();
        }
    }

    private String extractWeeklyEmotionTitle(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode titleNode = root.path("weeklyEmotionSummary").path("title");
            return titleNode.isMissingNode() ? null : titleNode.asText(null);
        } catch (Exception e) {
            log.warn("리포트 content에서 weeklyEmotionSummary.title 파싱 실패", e);
            return null;
        }
    }

    private Map<String, List<String>> buildWeeklyEmotionsMap(List<Record> records) {
        Map<String, List<String>> emotionsMap = new LinkedHashMap<>();
        for (String day : DAY_ORDER) {
            emotionsMap.put(day, new ArrayList<>());
        }

        for (Record record : records) {
            DayOfWeek dayOfWeek = record.getCreatedAt().getDayOfWeek();
            String dayKey = DAY_OF_WEEK_LABEL.get(dayOfWeek);
            if (dayKey != null) {
                List<String> dayEmotions = record.getEmotions().stream()
                        .map(e -> e.getDisplayValue())
                        .toList();
                emotionsMap.get(dayKey).addAll(dayEmotions);
            }
        }

        return emotionsMap;
    }
}
