package dalgrock.playlist.controller;

import dalgrock.playlist.controller.dto.request.CreateRecordRequest;
import dalgrock.playlist.controller.dto.request.UpdateRecordRequest;
import dalgrock.playlist.core.exception.InvalidInputValueException;
import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.RecordService;
import dalgrock.playlist.service.WeeklyService;
import dalgrock.playlist.service.dto.command.CreateRecordCommand;
import dalgrock.playlist.service.dto.command.CreateRecordMusicCommand;
import dalgrock.playlist.service.dto.command.UpdateRecordCommand;
import dalgrock.playlist.service.dto.response.CreateRecordResponse;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import dalgrock.playlist.service.dto.response.GetRecordResponse;
import dalgrock.playlist.service.dto.response.GetWeeklyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Record", description = "내 기록 관리 API")
@Validated
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/records")
public class RecordControllerV1 {

    private final RecordService recordService;
    private final WeeklyService weeklyService;

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
                        m.thumbnail(),
                        m.genre()
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

    @Operation(summary = "월별 기록 조회 (기록 전체보기)", description = "year, month 기준 해당 달의 월별 레코드를 조회합니다")
    @GetMapping("/monthly")
    public GetWeeklyResponse getMonthlyRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @Min(1900) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        return weeklyService.getWeeklyRecords(principal.userId(), year, month);
    }

    @Operation(
            summary = "내 기록 수정",
            description = """
                    type에 해당하는 필드만 수정합니다. 한 번에 하나의 필드만 수정 가능합니다.
                    
                    **type별 data 스키마:**
                    | type | data 스키마 | 예시 |
                    |------|-------------|------|
                    | content | string | `"오늘의 기록 내용"` |
                    | emotions | string[] | `["행복", "설렘"]` |
                    | situations | string[] | `["출퇴근", "카페"]` |
                    | musics | object[] | `[{ "title": "곡명", "artist": "아티스트", "thumbnail": "https://...", "genre": "팝" }]` |
                    
                    musics 수정 시: Music 테이블에 없으면 추가하고, record 썸네일은 첫 곡 썸네일로 갱신됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateRecordRequest.class),
                            examples = {
                                    @ExampleObject(name = "content 수정", value = "{\"type\":\"content\",\"data\":\"오늘의 기록 내용입니다.\"}"),
                                    @ExampleObject(name = "emotions 수정", value = "{\"type\":\"emotions\",\"data\":[\"행복\",\"설렘\",\"평온\"]}"),
                                    @ExampleObject(name = "situations 수정", value = "{\"type\":\"situations\",\"data\":[\"출퇴근\",\"카페\"]}"),
                                    @ExampleObject(name = "musics 수정", value = "{\"type\":\"musics\",\"data\":[{\"title\":\"곡제목\",\"artist\":\"아티스트명\",\"thumbnail\":\"https://example.com/thumb.png\",\"genre\":\"팝\"}]}"),
                            }
                    )
            )
    )
    @PatchMapping("/{recordId}")
    public GetRecordDetailResponse updateRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("recordId") Long recordId,
            @RequestBody @Valid UpdateRecordRequest request
    ) {
        UpdateRecordCommand command = toUpdateCommand(request);
        return recordService.updateRecord(principal.userId(), recordId, command);
    }

    @Operation(summary = "내 기록 삭제", description = "기록을 소프트 삭제합니다")
    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("recordId") Long recordId
    ) {
        recordService.deleteRecord(principal.userId(), recordId);
    }

    private UpdateRecordCommand toUpdateCommand(UpdateRecordRequest request) {
        String type = request.type().toLowerCase();
        JsonNode data = request.data();

        return switch (type) {
            case "content" -> {
                if (data.isArray() || data.isObject()) {
                    throw new InvalidInputValueException();
                }
                yield new UpdateRecordCommand(type, null, null, null, data.asText(null));
            }
            case "emotions" -> new UpdateRecordCommand(
                    type, null,
                    data.isArray() ? parseStringList(data) : List.of(),
                    null, null
            );
            case "situations" -> new UpdateRecordCommand(
                    type, null, null,
                    data.isArray() ? parseStringList(data) : List.of(),
                    null
            );
            case "musics" -> new UpdateRecordCommand(
                    type,
                    data.isArray() ? parseMusicList(data) : List.of(),
                    null, null, null
            );
            default -> throw new InvalidInputValueException();
        };
    }

    private List<String> parseStringList(JsonNode arr) {
        List<String> result = new ArrayList<>();
        arr.forEach(node -> result.add(node.asText()));
        return result;
    }

    private List<CreateRecordMusicCommand> parseMusicList(JsonNode arr) {
        List<CreateRecordMusicCommand> result = new ArrayList<>();
        arr.forEach(node -> {
            if (!node.isObject()) {
                throw new InvalidInputValueException();
            }
            result.add(new CreateRecordMusicCommand(
                    node.has("title") ? node.get("title").asText() : "",
                    node.has("artist") ? node.get("artist").asText() : "",
                    node.has("thumbnail") ? node.get("thumbnail").asText(null) : null,
                    node.has("genre") ? node.get("genre").asText(null) : null
            ));
        });
        return result;
    }
}
