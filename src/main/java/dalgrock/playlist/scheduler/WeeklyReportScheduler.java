package dalgrock.playlist.scheduler;

import dalgrock.playlist.infrastructure.deepseek.DeepSeekReportClient;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.User;
import dalgrock.playlist.service.WeeklyReportGenerationService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주간 리포트 자동 생성 스케줄러
 * 매주 일요일 18:00에 실행되어 모든 사용자의 주간 리포트를 생성합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WeeklyReportScheduler {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final WeeklyReportGenerationService weeklyReportGenerationService;
    private final DeepSeekReportClient deepSeekReportClient;

    @Scheduled(cron = "0 0 18 * * SUN", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        if (!deepSeekReportClient.isConfigured()) {
            log.warn("주간 리포트 스킵: deepseek.api-key가 설정되지 않았습니다.");
            return;
        }
        try {
            List<User> users = userRepository.findAll();
            log.info("이번주 분석 유저수: {}", users.size());

            LocalDate today = ZonedDateTime.now(APP_ZONE).toLocalDate();
            int year = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getYear();
            int month = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getMonthValue();
            int week = (today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getDayOfMonth() - 1) / 7 + 1;

            int successCount = 0;
            int failureCount = 0;

            for (User user : users) {
                try {
                    if (weeklyReportGenerationService.generateReport(user.getId(), year, month, week).isPresent()) {
                        successCount++;
                        log.debug("주간 리포트 생성 완료, 사용자ID: {}", user.getId());
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("주간 리포트 생성 실패, 사용자ID: {}", user.getId(), e);
                }
            }
            log.info("주간 레코드 생성. 성공: {}, 실패: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("주간 리포트 생성 중 치명적 오류 발생", e);
        }
    }
}
