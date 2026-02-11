package dalgrock.playlist.infrastructure.spotify;

import dalgrock.playlist.infrastructure.spotify.dto.SpotifyTokenResponse;
import dalgrock.playlist.infrastructure.spotify.dto.TokenCache;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyAuthService {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final int BUFFER_SECONDS = 60;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final RestClient spotifyRestClient;
    private final AtomicReference<TokenCache> tokenCache = new AtomicReference<>();

    public synchronized String getAccessToken() {
        TokenCache current = tokenCache.get();

        if (isTokenValid(current)) {
            log.debug("Using cached Spotify access token");
            return current.token();
        }

        log.info("Fetching new Spotify access token");
        return fetchAndCacheNewToken();
    }

    private boolean isTokenValid(TokenCache cache) {
        if (cache == null) {
            return false;
        }

        Instant expiryWithBuffer = cache.expiryTime().minusSeconds(BUFFER_SECONDS);
        boolean isValid = Instant.now().isBefore(expiryWithBuffer);

        if (!isValid) {
            log.debug("Token will expire soon. Current time: {}, Expiry: {}",
                    Instant.now(), cache.expiryTime());
        }

        return isValid;
    }

    private String fetchAndCacheNewToken() {
        String authHeader = createBasicAuthHeader();

        SpotifyTokenResponse response = spotifyRestClient.post()
                .uri(TOKEN_URL)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(SpotifyTokenResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to fetch Spotify access token");
        }

        Instant expiryTime = Instant.now().plusSeconds(response.expiresIn());
        TokenCache newCache = new TokenCache(response.accessToken(), expiryTime);
        tokenCache.set(newCache);

        log.info("New Spotify token cached. Expires at: {}", expiryTime);
        return newCache.token();
    }

    private String createBasicAuthHeader() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
