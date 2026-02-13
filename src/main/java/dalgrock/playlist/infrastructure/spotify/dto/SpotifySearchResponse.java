package dalgrock.playlist.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SpotifySearchResponse(
        TracksResponse tracks
) {

    public record TracksResponse(
            List<ItemResponse> items
    ) {
    }

    public record ItemResponse(
            String id,
            String name,
            List<ArtistResponse> artists,
            AlbumResponse album,
            @JsonProperty("external_urls")
            ExternalUrlsResponse externalUrls
    ) {
    }

    public record ArtistResponse(
            String id,
            String name
    ) {
    }

    public record AlbumResponse(
            String name,
            List<ImageResponse> images
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
