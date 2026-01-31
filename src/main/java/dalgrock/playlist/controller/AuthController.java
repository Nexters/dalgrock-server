package dalgrock.playlist.controller;

import dalgrock.playlist.controller.exception.ErrorResponse;
import dalgrock.playlist.core.exception.UserNotFoundException;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.User;
import dalgrock.playlist.model.UserPrincipal;
import dalgrock.playlist.service.dto.response.LoginResponse;
import dalgrock.playlist.service.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "인증 및 권한 관리 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @Operation(
            summary = "카카오 로그인",
            description = """
                    카카오 OAuth2를 통한 소셜 로그인을 수행합니다.
                    
                    **실제 로그인 플로우:**
                    1. 클라이언트는 GET /oauth2/authorization/kakao 로 리다이렉트
                    2. 카카오 로그인 페이지로 이동
                    3. 사용자 인증 후 콜백 처리
                    4. JWT 토큰이 포함된 응답 반환
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (지원하지 않는 Provider 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/oauth2/authorization/kakao")
    public LoginResponse kakaoLogin() {
        throw new UnsupportedOperationException(
                "이 엔드포인트는 Swagger 명세 제공용이며, 실제로는 Spring Security가 /oauth2/authorization/kakao 를 처리합니다.");
    }

    @Operation(
            summary = "현재 로그인한 사용자 정보 조회",
            description = "JWT 토큰으로 인증된 사용자의 정보를 반환합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/api/auth/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(UserNotFoundException::new);

        return UserResponse.from(user);
    }
}
