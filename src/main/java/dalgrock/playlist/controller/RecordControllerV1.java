package dalgrock.playlist.controller;

import dalgrock.playlist.controller.dto.request.CreateRecordRequest;
import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.RecordService;
import dalgrock.playlist.service.dto.command.CreateRecordCommand;
import dalgrock.playlist.service.dto.command.CreateRecordMusicCommand;
import dalgrock.playlist.service.dto.response.CreateRecordResponse;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import dalgrock.playlist.service.dto.response.GetRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Record", description = "내 기록 관리 API")
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/records")
public class RecordControllerV1 {

    private final RecordService recordService;

    @Operation(summary = "내 기록 상세 조회", description = "내 기록 ID로 기록을 상세 조회합니다")
    @GetMapping("/detail/{recordId}")
    public GetRecordDetailResponse getRecordDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("recordId") Long recordId
    ) {
        return recordService.getRecordDetail(principal.userId(), recordId);
    }

    @Operation(summary = "내 기록 추가", description = "오늘의 내 기록을 추가합니다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateRecordResponse createRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateRecordRequest request
    ) {
        CreateRecordCommand command = toCommand(request);
        return recordService.createRecord(principal.userId(), command);
    }

    private CreateRecordCommand toCommand(CreateRecordRequest request) {
        List<CreateRecordMusicCommand> musicCommands = request.musics().stream()
                .map(m -> new CreateRecordMusicCommand(
                        m.title(),
                        m.artist(),
                        m.thumbnail()
                ))
                .collect(Collectors.toList());
        return new CreateRecordCommand(
                musicCommands,
                request.emotions(),
                request.content(),
                request.situations(),
                request.location()
        );
    }

    @GetMapping()
    public GetRecordResponse getRecords(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return recordService.getRecords(principal.userId());
    }
}
