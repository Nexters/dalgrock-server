package dalgrock.playlist.infrastructure.spotify.dto;

import java.time.Instant;

public record TokenCache(
        String token,
        Instant expiryTime
) {
}
