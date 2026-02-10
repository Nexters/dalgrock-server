package dalgrock.playlist.controller;

import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.RecordService;
import dalgrock.playlist.service.dto.command.CreateRecordCommand;
import dalgrock.playlist.service.dto.response.CreateRecordResponse;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import dalgrock.playlist.service.dto.response.GetRecordResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/")
    public CreateRecordResponse createRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateRecordCommand command
    ) {
        return recordService.createRecord(principal.userId(), command);
    }

    @GetMapping("/")
    public GetRecordResponse getRecords(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return recordService.getRecords(principal.userId());
    }
}
