package dalgrock.playlist.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SpotifySearchResponse(
        List<ItemResponse> itemResponses
) {

    public record ItemResponse(
            String id,
            String name,
            List<ArtistResponse> artistResponses,
            AlbumResponse albumResponse,
            @JsonProperty("external_urls")
            ExternalUrlsResponse externalUrlsResponse
    ) {
    }

    public record ArtistResponse(
            String id,
            String name
    ) {
    }

    public record AlbumResponse(
            String name,
            List<ImageResponse> imageResponses
    ) {
    }

    public record ImageResponse(
            String url,
            int height,
            int width
    ) {
    }

    public record ExternalUrlsResponse(
            String spotify
    ) {
    }
}
