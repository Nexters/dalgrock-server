package dalgrock.playlist.scheduler;

import dalgrock.playlist.infrastructure.deepseek.DeepSeekReportClient;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.User;
import dalgrock.playlist.service.WeeklyReportDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주간 리포트 자동 생성 스케줄러
 * 매주 일요일 18:00에 실행되어 모든 사용자의 주간 리포트를 생성합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WeeklyReportScheduler {

    private static final String DATA_FOR_CREATE_REPORT = 
    """
              ## 생성해야 할 항목
              ### 1. overallSummary.title (주간 요약 타이틀)
              
              - 이번 주를 한 문장으로 요약 (최대 36자, 공백 포함)
              - 주요 감정과 음악 장르를 포함
              - 따뜻하고 공감적인 톤
              - 이모지 사용 가능 (선택사항)
              - 예시: "잔잔하고 외로웠던 이번 주, R&B와 함께했어요 ✨"
              
              ### 2. overallSummary.summaryTags (요약 태그)
              
              - 정확히 2개의 태그 생성
              - 각 태그는 1~2단어
              - 감정 상태나 음악 청취 패턴을 나타냄
              - 예시: ["위로", "전환"]
              
              ### 3. weeklyEmotionSummary.title (이번 주 감정 요약)
              
              - 일별 감정 변화를 한 문장으로 요약
              - 격려적이고 공감적인 톤
              - 감정 회복이나 변화 과정을 강조
              - 예시: "우울한 감정을 열심히 회복시켰어요"
              
              ### 4. emotionGenreDescriptions (감정-장르 분석 설명)
              
              - 각 감정-장르 조합에 대한 맞춤형 메시지
              - "당신은 ~였어요" 형식
              - 긍정적이고 격려적인 톤
              - 해당 감정을 그 장르의 음악으로 해소한 사용자의 행동을 긍정적으로 해석
              - 예시: "복잡미묘한 감정을 힙한 음악으로 해소한 당신은 분위기를 순식간에 바꾸는 반전 메이커였어요!"
              
              
              ### 5. weeklyComparison.summary (지난 주와의 비교 요약)
              
              - 지난 주 대비 변화를 한 문장으로 요약
              - 감정과 장르를 모두 포함
              - 중립적이면서도 관찰적인 톤
              - 예시: "지난 주에 비해 우울감을 더 많이 느끼고 팝 장르 음악을 많이 들은 한 주였어요"
              
              ### 6. weeklyComparison.nextWeekSuggestion (다음 주 제안)
              
              - 이번 주 분석을 바탕으로 다음 주 음악 감상 제안
              - "~하는 건 어떨까요?" 형식 권장
              - 격려적이고 제안하는 톤
              - 구체적이면서도 부드러운 표현
              - 예시: "다음 주에는 신나는 음악으로 기분을 전환해 보는 건 어떨까요?"
              """;

    private static final String OUTPUT_FORMAT = """
              ## 출력 형식
              
              다음 JSON 형식으로 응답해주세요:
              - overallSummary: { title, summaryTags }
              - weeklyEmotionSummary: { title }
              - emotionGenreDescriptions: [ { emotion, description }, ... ]
              - weeklyComparison: { summary, nextWeekSuggestion }
              """;

    private final UserRepository userRepository;
    private final WeeklyReportDataService weeklyReportDataService;
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

            int successCount = 0;
            int failureCount = 0;

            for (User user : users) {
                try {
                    String reportContent = processUserReport(user);
                    if (reportContent != null) {
                        successCount++;
                        log.debug("주간 리포트 생성 완료, 사용자ID: {}, 응답 길이: {}", user.getId(), reportContent.length());
                    }
                } catch (Exception e) {
                    failureCount++;
                }
            }
            log.info("주간 레코드 생성. 성공: {}, 실패: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Fatal error during weekly report generation", e);
        }
    }

    /**
     * 한 사용자에 대해 주간 분석 데이터로 DeepSeek를 호출하고, 리포트 문자열을 반환합니다.
     * (제공해주신 DeepSeek 코드와 동일한 결과를 반환합니다.)
     */
    private String processUserReport(User user) {
        try {
            String dataForAnalysis = weeklyReportDataService.buildDataForAnalysis(user.getId());
            String systemMessage = "당신은 음악 기록 서비스의 주간 분석 리포트를 생성하는 AI 어시스턴트입니다.\n"
                    + "사용자의 이번 주 음악 기록 데이터를 분석하여 따뜻하고 공감적인 문장들을 생성해주세요.\n"
                    + dataForAnalysis + "\n"
                    + DATA_FOR_CREATE_REPORT + "\n"
                    + OUTPUT_FORMAT;

            String reportContent = deepSeekReportClient.ask(systemMessage);
            return reportContent;
        } catch (Exception e) {
            log.error("분석 실패, 사용자ID: {}", user.getId(), e);
            throw e;
        }
    }
}
