package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.model.Music;

public record MusicSearchResponse(
        String spotifyId,
        String title,
        String artist
) {

    public static MusicSearchResponse from(Music music) {
        return new MusicSearchResponse(
                music.getSpotifyId(),
                music.getTitle(),
                music.getArtist()
        );
    }
}
