package dalgrock.playlist.infrastructure.client;

import dalgrock.playlist.infrastructure.spotify.SpotifyAuthService;
import dalgrock.playlist.infrastructure.spotify.dto.MusicCommand;
import dalgrock.playlist.infrastructure.spotify.dto.SpotifyArtistResponse;
import dalgrock.playlist.infrastructure.spotify.dto.SpotifySearchResponse;
import dalgrock.playlist.infrastructure.spotify.dto.SpotifySearchResponse.ItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyMusicSearchClient implements MusicSearchClient {

    private static final String SPOTIFY_API_HOST = "api.spotify.com";
    private static final String SEARCH_PATH = "/v1/search";
    private static final String ARTISTS_PATH = "/v1/artists";
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
                log.warn("Spotify API 응답 없음");
                return List.of();
            }

            List<ItemResponse> items = response.tracks().items();
            log.info("Spotify API 검색 완료: {}개 결과", items.size());

            Map<String, String> artistGenreMap = fetchArtistGenres(items, accessToken);
            return mapToDomainModels(items, artistGenreMap);
        } catch (Exception e) {
            log.error("Spotify API 검색 실패", e);
            return List.of();
        }
    }

    @Override
    public String getProviderName() {
        return "Spotify";
    }

    private Map<String, String> fetchArtistGenres(List<ItemResponse> items, String accessToken) {
        List<String> artistIds = items.stream()
                .filter(item -> item.artists() != null && !item.artists().isEmpty())
                .map(item -> item.artists().getFirst().id())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (artistIds.isEmpty()) {
            return Map.of();
        }

        String ids = String.join(",", artistIds);

        try {
            SpotifyArtistResponse artistResponse = spotifyRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(SPOTIFY_API_HOST)
                            .path(ARTISTS_PATH)
                            .queryParam("ids", ids)
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(SpotifyArtistResponse.class);

            if (artistResponse == null || artistResponse.artists() == null) {
                return Map.of();
            }

            return artistResponse.artists().stream()
                    .filter(Objects::nonNull)
                    .filter(artist -> artist.id() != null)
                    .filter(artist -> artist.genres() != null && !artist.genres().isEmpty())
                    .collect(Collectors.toMap(
                            SpotifyArtistResponse.ArtistDetail::id,
                            artist -> artist.genres().getFirst(),
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("Spotify Artists API 호출 실패", e);
            return Map.of();
        }
    }

    private List<MusicCommand> mapToDomainModels(List<ItemResponse> items, Map<String, String> artistGenreMap) {
        return items.stream()
                .map(item -> mapToDomainModel(item, artistGenreMap))
                .toList();
    }

    private MusicCommand mapToDomainModel(ItemResponse item, Map<String, String> artistGenreMap) {
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

        String genre = Optional.ofNullable(item.artists())
                .filter(a -> !a.isEmpty())
                .map(a -> artistGenreMap.get(a.getFirst().id()))
                .orElse(null);

        return new MusicCommand(
                item.id(),
                item.name(),
                artistName,
                thumbnail,
                spotifyUrl,
                genre
        );
    }
}
