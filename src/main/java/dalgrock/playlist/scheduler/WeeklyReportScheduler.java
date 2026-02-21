package dalgrock.playlist.scheduler;

import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.User;
import dalgrock.playlist.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주간 리포트 자동 생성 스케줄러
 * <p>
 * 매주 일요일 18:00에 실행되어 모든 사용자의 주간 리포트를 생성합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WeeklyReportScheduler {

    private final UserRepository userRepository;
    private final WeeklyReportService weeklyReportService;

    @Scheduled(cron = "0 0 18 * * SUN", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        try {
            List<User> users = userRepository.findAll();
            log.info("이번주 분석 유저수: {}", users.size());

            int successCount = 0;
            int failureCount = 0;

            for (User user : users) {
                try {
                    processUserReport(user);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                }
            }
            log.info("주간 레코드 생성. 성공: {}, 실패: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Fatal error during weekly report generation", e);
        }
    }

    private void processUserReport(User user) {
        try {
            // weeklyReportService.generateWeeklyReport(user.getId());
        } catch (Exception e) {
            log.error("분석 실패, 사용자ID: {}", user.getId(), e);
            throw e;
        }
    }
}
