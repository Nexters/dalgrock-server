package dalgrock.playlist.controller.dto.request;

import tools.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(
        description = "기록 수정 요청. type에 따라 data 스키마가 달라집니다. 한 번에 하나의 필드만 수정 가능합니다.",
        requiredProperties = {"type", "data"}
)
public record UpdateRecordRequest(
        @NotBlank(message = "type is required")
        @Pattern(regexp = "^(content|emotions|situations|musics)$", message = "type must be one of: content, emotions, situations, musics")
        @Schema(
                description = "수정할 필드 타입",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"content", "emotions", "situations", "musics"},
                example = "content"
        )
        String type,

        @NotNull(message = "data is required")
        @Schema(
                description = """
                        type에 따른 data 스키마:
                        - **content**: string (기록 본문)
                        - **emotions**: string[] (감정 목록)
                        - **situations**: string[] (상황 목록)
                        - **musics**: object[] (음악 목록, 각 항목: { title, artist, thumbnail })
                        """,
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "오늘의 기록 내용"
        )
        JsonNode data
) {
}
