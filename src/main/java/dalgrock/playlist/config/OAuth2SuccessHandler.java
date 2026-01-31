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

    private void redirectToCallback(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString("/oauth-callback.html")
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
