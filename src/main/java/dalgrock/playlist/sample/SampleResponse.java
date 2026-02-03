package dalgrock.playlist.sample;

import io.swagger.v3.oas.annotations.media.Schema;

public record SampleResponse(
        @Schema(description = "샘플 식별자", example = "1")
        Long id,
        @Schema(description = "샘플 닉네임", example = "달그락", nullable = false)
        String nickname,
        @Schema(description = "샘플 권한", implementation = SampleRole.class)
        SampleRole role
) {
}
