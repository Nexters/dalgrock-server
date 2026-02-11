package dalgrock.playlist.controller;

import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.RecordService;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Record", description = "내 기록 관리 API")
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/records")
public class RecordControllerV1 {

    private final RecordService recordService;

    @GetMapping("/detail/{recordId}")
    public GetRecordDetailResponse getRecordDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("recordId") Long recordId
    ) {
        return recordService.getRecordDetail(principal.userId(), recordId);
    }
}
