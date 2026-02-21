package dalgrock.playlist.config;

import dalgrock.playlist.core.exception.UserNotFoundException;
import dalgrock.playlist.core.jwt.JwtTokenProvider;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String LOCAL_HOST = "http://localhost:5173";
    private static final String LOCAL_HOST_PATTERN = "localhost:5173";

    @Value("${app.auth.redirect-uri}")
    private String targetUrl;

    @Value("${app.auth.samesite-cookie}")
    private String sameSite;

    @Value("${app.auth.secure-cookie}")
    private Boolean secureHttp;

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerId = extractProviderId(oAuth2User);
        User user = findUser(providerId);
        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getRole().name());

        redirectToCallback(request, response, accessToken);
    }

    private String extractProviderId(OAuth2User oAuth2User) {
        return String.valueOf(oAuth2User.getAttributes().get("id"));
    }

    private User findUser(String providerId) {
        return userRepository.findByProviderAndProviderId(OauthProvider.KAKAO, providerId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * 요청의 Origin 또는 Referer 헤더를 확인하여 로컬 환경 여부를 판단합니다.
     */
    private boolean isLocalRequest(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        // Origin 헤더 우선 확인
        if (origin != null && origin.contains(LOCAL_HOST_PATTERN)) {
            return true;
        }

        // Referer 헤더 확인
        return referer != null && referer.contains(LOCAL_HOST_PATTERN);
    }

    /**
     * 쿠키를 설정하고 리다이렉트합니다.
     * 요청 Origin이 localhost인 경우 쿠키 설정을 로컬 환경에 맞게 조정합니다.
     * 리다이렉트 URL은 항상 설정된 redirect-uri를 사용합니다.
     */
    private void redirectToCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            String accessToken
    ) throws IOException {
        boolean isLocalEnvironment = isLocalRequest(request);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("access_token", accessToken)
                .path("/")
                .httpOnly(true)
                .maxAge(3600);

        if (isLocalEnvironment) {
            // 로컬 환경: http 프로토콜, SameSite=Lax, domain 설정 없음
            cookieBuilder
                    .secure(false)
                    .sameSite("Lax");
        } else {
            // 배포 환경: 설정값 사용 (https, SameSite=None)
            cookieBuilder
                    .secure(secureHttp)
                    .sameSite(sameSite);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
