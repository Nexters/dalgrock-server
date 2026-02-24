package dalgrock.playlist.config;

import dalgrock.playlist.core.exception.UserNotFoundException;
import dalgrock.playlist.core.jwt.JwtTokenProvider;
import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
        HttpSession session = request.getSession(false);
        if (session != null) {
            String savedOrigin = (String) session.getAttribute(OAuth2LoginOriginFilter.SESSION_KEY);
            if (savedOrigin != null) {
                session.removeAttribute(OAuth2LoginOriginFilter.SESSION_KEY);
                logger.info("세션에서 프론트엔드 origin 복원: " + savedOrigin);
                return buildRedirectUrl(savedOrigin);
            }
        }

        throw new IllegalStateException("세션에 저장된 프론트엔드 origin이 없어 리다이렉트 URL을 결정할 수 없습니다.");
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
        String domain = null;
        if (redirectUrl.contains("pliview.kr")) {
            domain = ".pliview.kr";
        } else if (redirectUrl.contains("localhost")) {
            domain = "localhost";
        }

        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                .path("/")
                .httpOnly(false)
                .maxAge(3600000)
                .secure(true)
                .sameSite("None")
                .domain(domain)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        logger.info("최종 Set-Cookie 헤더: " + cookie.toString());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
