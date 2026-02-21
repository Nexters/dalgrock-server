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

        logger.info("user: " + user);
        logger.info("accessToken: " + accessToken);

        String redirectUrl = determineRedirectUrl(request);
        redirectToCallback(request, response, accessToken, redirectUrl);
    }

    private String determineRedirectUrl(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        logger.info("origin: " + origin);
        logger.info("referer: " + referer);

        if (origin != null && origin.contains(LOCAL_HOST_PATTERN)) {
            return buildRedirectUrl(LOCAL_HOST);
        }

        if (referer != null && referer.contains(LOCAL_HOST_PATTERN)) {
            return buildRedirectUrl(LOCAL_HOST);
        }

        return targetUrl;
    }

    /**
     * UriComponentsBuilder를 사용하여 안전하게 URL을 생성합니다.
     */
    private String buildRedirectUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/kakao/callback")
                .build()
                .toUriString();
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
                .httpOnly(true)
                .maxAge(3600);

        if (isLocalEnvironment) {
            cookieBuilder
                    .domain("localhost")
                    .secure(false)
                    .sameSite("Lax");
        } else {
            cookieBuilder
                    .secure(secureHttp)
                    .sameSite(sameSite);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        long maxAgeSeconds = cookie.getMaxAge() != null ? cookie.getMaxAge().toSeconds() : -1;
        logger.info("Set-Cookie 추가됨: name=" + cookie.getName() + ", path=" + cookie.getPath()
                + ", maxAge=" + maxAgeSeconds + "s, redirectUrl=" + redirectUrl + ", isLocal=" + isLocalEnvironment);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
