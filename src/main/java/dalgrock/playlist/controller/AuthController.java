package dalgrock.playlist.controller;

import dalgrock.playlist.controller.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "인증 및 권한 관리 API")
@RestController
@RequiredArgsConstructor
public class AuthController {

    @Operation(
            summary = "카카오 로그인 시작",
            description = """
                    카카오 OAuth2 로그인 플로우를 시작합니다.
                    
                    **실제 동작:**
                    - Spring Security가 이 요청을 가로채서 처리합니다
                    - 사용자를 카카오 로그인 페이지로 리다이렉트합니다
                    
                    **전체 플로우:**
                    1. 클라이언트가 이 엔드포인트로 접속 (GET)
                    2. 카카오 로그인 페이지로 리다이렉트 (302)
                    3. 사용자가 카카오에서 인증
                    4. 카카오가 /login/oauth2/code/kakao 로 콜백
                    5. 인증 성공 시 {origin}/ 로 리다이렉트
                    
                    **주의:** 이 메서드는 Swagger 문서화용이며 실제로는 실행되지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 페이지로 리다이렉트"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/oauth2/authorization/kakao")
    public void loginWithKakao() {
    }
}
