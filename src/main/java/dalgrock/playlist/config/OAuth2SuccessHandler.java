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

        redirectToCallback(request, response, accessToken);
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
            String accessToken
    ) throws IOException {
        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                .path("/")
                .httpOnly(true)
                .secure(secureHttp)
                .sameSite(sameSite)    // Lax, None
                .maxAge(3600)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
