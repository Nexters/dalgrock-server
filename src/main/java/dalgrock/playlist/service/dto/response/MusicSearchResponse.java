package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.infrastructure.spotify.dto.MusicCommand;
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

    public static MusicSearchResponse from(MusicCommand command) {
        return new MusicSearchResponse(
                command.spotifyId(),
                command.title(),
                command.artist()
        );
    }
}
