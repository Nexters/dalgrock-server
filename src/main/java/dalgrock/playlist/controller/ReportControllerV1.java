package dalgrock.playlist.controller;

import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.ReportService;
import dalgrock.playlist.service.dto.response.GetReportDetailResponse;
import dalgrock.playlist.service.dto.response.GetReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "주간 리포트 API")
@Validated
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/reports")
public class ReportControllerV1 {

    private final ReportService reportService;

    @Operation(summary = "월별 리포트 조회", description = "year, month 기준 해당 달의 주차별 리포트를 조회합니다")
    @GetMapping
    public GetReportResponse getMonthlyReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @Min(1900) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        return reportService.getMonthlyReport(principal.userId(), year, month);
    }

    @Operation(summary = "주간 리포트 상세 조회", description = "reportId에 해당하는 주간 리포트 상세 정보를 조회합니다")
    @GetMapping("/{reportId}")
    public GetReportDetailResponse getReportDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return reportService.getReportDetail(principal.userId(), reportId);
    }
}
