package dalgrock.playlist.config;

import dalgrock.playlist.core.jwt.JwtTokenProvider;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. OAuth2User의 attributes에서 providerId 추출
        String providerId = String.valueOf(oAuth2User.getAttributes().get("id"));

        // 2. DB에서 실제 User 엔티티 조회
        User user = userRepository.findByProviderAndProviderId(OauthProvider.KAKAO, providerId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 3. User의 실제 ID(PK)와 role로 JWT 생성
        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getRole().name());

        // 4. 콜백 페이지로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("/oauth-callback.html")
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
