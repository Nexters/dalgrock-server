package dalgrock.playlist.config;

import dalgrock.playlist.core.exception.UserNotFoundException;
import dalgrock.playlist.core.jwt.JwtTokenProvider;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String LOCAL_HOST = "https://localhost:5173";
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

        logger.info("user: " + user);
        logger.info("accessToken: " + accessToken);

        String redirectUrl = determineRedirectUrl(request);
        redirectToCallback(request, response, accessToken, redirectUrl);
    }

    /**
     * 요청 Origin 또는 Referer에서 호스트를 추출하여 리다이렉트 URL을 생성합니다.
     * 동적으로 호스트 + /auth/kakao/callback 경로를 반환합니다.
     */
    private String determineRedirectUrl(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        logger.info("origin: " + origin);
        logger.info("referer: " + referer);

        // Origin 헤더 우선 사용
        if (origin != null && !origin.isBlank()) {
            return buildRedirectUrl(origin);
        }

        // Referer 헤더에서 호스트 추출
        if (referer != null && !referer.isBlank()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                String host = uri.getScheme() + "://" + uri.getAuthority();
                logger.info("Referer에서 추출한 호스트: " + host);
                return buildRedirectUrl(host);
            } catch (java.net.URISyntaxException e) {
                logger.warn("Referer 파싱 실패: " + referer, e);
            }
        }

        // 기본값: 설정된 redirect-uri 사용
        logger.info("기본 redirect-uri 사용: " + targetUrl);
        return targetUrl;
    }

    /**
     * UriComponentsBuilder를 사용하여 안전하게 URL을 생성합니다.
     * baseUrl + /auth/kakao/callback 경로를 반환합니다.
     */
    private String buildRedirectUrl(String baseUrl) {
        String redirectUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/kakao/callback")
                .build()
                .toUriString();
        logger.info("생성된 리다이렉트 URL: " + redirectUrl);
        return redirectUrl;
    }

    private String extractProviderId(OAuth2User oAuth2User) {
        return String.valueOf(oAuth2User.getAttributes().get("id"));
    }

    private User findUser(String providerId) {
        return userRepository.findByProviderAndProviderId(OauthProvider.KAKAO, providerId)
                .orElseThrow(UserNotFoundException::new);
    }

    private void redirectToCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            String accessToken,
            String redirectUrl
    ) throws IOException {
        boolean isLocalEnvironment = redirectUrl.contains(LOCAL_HOST_PATTERN);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("access_token", accessToken)
                .path("/")
                .httpOnly(false)
                .secure(true)
                .maxAge(3600)
                .domain(".pliview.kr");

        if (isLocalEnvironment) {
            cookieBuilder
                    .sameSite("None");
        } else {
            cookieBuilder
                    .secure(secureHttp)
                    .sameSite(sameSite);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logger.info("cookie" + cookie);
        long maxAgeSeconds = cookie.getMaxAge() != null ? cookie.getMaxAge().toSeconds() : -1;
        logger.info("Set-Cookie 추가됨: name=" + cookie.getName() + ", path=" + cookie.getPath()
                + ", maxAge=" + maxAgeSeconds + "s, redirectUrl=" + redirectUrl + ", isLocal=" + isLocalEnvironment);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
