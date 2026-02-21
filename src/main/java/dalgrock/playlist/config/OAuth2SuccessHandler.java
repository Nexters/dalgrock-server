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

        if (origin != null && !origin.isBlank()) {
            return buildRedirectUrl(origin);
        }

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
        logger.info("기본 redirect-uri 사용: " + targetUrl);
        return targetUrl;
    }

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
        CookieConfig cookieConfig = extractCookieConfig(redirectUrl);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("access_token", accessToken)
                .path("/")
                .httpOnly(false)
                .maxAge(3600)
                .secure(cookieConfig.secure());

        if (cookieConfig.domain() != null) {
            cookieBuilder.domain(cookieConfig.domain());
        }

        if (cookieConfig.sameSite() != null) {
            cookieBuilder.sameSite(cookieConfig.sameSite());
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private CookieConfig extractCookieConfig(String redirectUrl) {
        try {
            java.net.URI uri = new java.net.URI(redirectUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            boolean isHttps = "https".equalsIgnoreCase(scheme);

            boolean secure = isHttps;

            String domain = null;
            if ("localhost".equalsIgnoreCase(host)) {
                domain = "localhost";
            }

            String sameSiteValue;
            if (!isHttps) {
                sameSiteValue = null;
            } else {
                sameSiteValue = sameSite;
            }
            return new CookieConfig(scheme, host, domain, secure, sameSiteValue);

        } catch (java.net.URISyntaxException e) {
            logger.warn("redirectUrl 파싱 실패, 기본값 사용: " + redirectUrl, e);
            return new CookieConfig("https", null, null, secureHttp, sameSite);
        }
    }

    private record CookieConfig(
            String scheme,
            String host,
            String domain,
            boolean secure,
            String sameSite
    ) {
    }
}
