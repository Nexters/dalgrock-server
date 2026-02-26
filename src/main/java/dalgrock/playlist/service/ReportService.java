package dalgrock.playlist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.ReportRepository;
import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.model.Report;
import dalgrock.playlist.model.ReportStatus;
import dalgrock.playlist.model.Weekly;
import dalgrock.playlist.service.dto.response.GetReportResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
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
}
