package dalgrock.playlist.controller;

import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.WeeklyService;
import dalgrock.playlist.service.dto.response.GetWeeklyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weekly", description = "월별 기록 조회 API")
@Validated
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/reports/monthly")
public class WeeklyControllerV1 {

    private final WeeklyService weeklyService;

    @Operation(summary = "주차별 기록 조회", description = "year, month 기준 해당 달의 주차별 레코드를 조회합니다")
    @GetMapping
    public GetWeeklyResponse getWeeklyRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @Min(1900) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        return weeklyService.getWeeklyRecords(principal.userId(), year, month);
    }
}
