package dalgrock.playlist.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 응답")
public record LoginResponse(
        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType
) {

    public static LoginResponse of(String accessToken) {
        return new LoginResponse(accessToken, "Bearer");
    }
}
