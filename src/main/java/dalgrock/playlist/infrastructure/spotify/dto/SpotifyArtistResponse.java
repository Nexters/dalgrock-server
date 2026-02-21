package dalgrock.playlist.infrastructure.spotify.dto;

import java.util.List;

public record SpotifyArtistResponse(
        List<ArtistDetail> artists
) {

    public record ArtistDetail(
            String id,
            List<String> genres
    ) {
    }
}
