package dalgrock.playlist.controller;

import dalgrock.playlist.core.jwt.JwtTokenProvider;
import dalgrock.playlist.model.Report;
import dalgrock.playlist.service.WeeklyReportGenerationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private static final String COOKIE_NAME = "access_token";

    private final JwtTokenProvider tokenProvider;
    private final WeeklyReportGenerationService weeklyReportGenerationService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * 지정한 year, month, week에 대해 주간 리포트를 생성합니다.
     * GET /test/weekly-report?userId=1&year=2025&month=2&week=4
     */
    @GetMapping("/weekly-report")
    public ResponseEntity<Map<String, Object>> generateWeeklyReport(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int week) {
        var reportOpt = weeklyReportGenerationService.generateReport(userId, year, month, week);
        return reportOpt
                .map(report -> ResponseEntity.ok(Map.<String, Object>of(
                        "success", true,
                        "reportId", report.getId(),
                        "content", report.getContent())))
                .orElse(ResponseEntity.ok(Map.of("success", false, "message", "해당 주차 데이터가 없거나 API 키가 설정되지 않았습니다.")));
    }

    @GetMapping("/token")
    public String generateToken(HttpServletResponse response) {
        String token = tokenProvider.createAccessToken(1L, "USER");

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMs / 1000));
        response.addCookie(cookie);

        return token;
    }
}
