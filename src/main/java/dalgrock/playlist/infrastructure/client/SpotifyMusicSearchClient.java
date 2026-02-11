package dalgrock.playlist.infrastructure.client;

import dalgrock.playlist.infrastructure.spotify.dto.MusicCommand;
import dalgrock.playlist.infrastructure.spotify.SpotifyAuthService;
import dalgrock.playlist.infrastructure.spotify.dto.SpotifySearchResponse;
import dalgrock.playlist.infrastructure.spotify.dto.SpotifySearchResponse.ItemResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyMusicSearchClient implements MusicSearchClient {

    private static final String SPOTIFY_API_HOST = "api.spotify.com";
    private static final String SEARCH_PATH = "/v1/search";
    private static final int SEARCH_LIMIT = 20;

    private final RestClient spotifyRestClient;
    private final SpotifyAuthService authService;

    @Override
    public List<MusicCommand> search(String keyword) {
        try {
            String accessToken = authService.getAccessToken();

            SpotifySearchResponse response = spotifyRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(SPOTIFY_API_HOST)
                            .path(SEARCH_PATH)
                            .queryParam("q", keyword)
                            .queryParam("type", "track")
                            .queryParam("limit", SEARCH_LIMIT)
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(SpotifySearchResponse.class);

            if (response == null || response.tracks() == null || response.tracks().items() == null) {
                log.warn("No results from Spotify API");
                return List.of();
            }

            log.info("Spotify API returned {} results", response.tracks().items().size());
            return mapToDomainModels(response.tracks().items());
        } catch (Exception e) {
            log.error("Failed to search from Spotify API", e);
            return List.of();
        }
    }

    @Override
    public String getProviderName() {
        return "Spotify";
    }

    private List<MusicCommand> mapToDomainModels(List<ItemResponse> items) {
        return items.stream()
                .map(this::mapToDomainModel)
                .toList();
    }

    private MusicCommand mapToDomainModel(ItemResponse item) {
        String artistName = item.artists() != null && !item.artists().isEmpty()
                ? item.artists().getFirst().name()
                : "Unknown Artist";

        String thumbnail = item.album() != null
                && item.album().images() != null
                && !item.album().images().isEmpty()
                ? item.album().images().getFirst().url()
                : null;

        String spotifyUrl = item.externalUrls() != null
                ? item.externalUrls().spotify()
                : null;

        return new MusicCommand(
                item.id(),
                item.name(),
                artistName,
                thumbnail,
                spotifyUrl
        );
    }
}
