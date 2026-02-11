package dalgrock.playlist.infrastructure.spotify.dto;

import dalgrock.playlist.model.Music;

public record MusicCommand(
        String spotifyId,
        String title,
        String artist,
        String thumbnail,
        String spotifyUrl
) {

    public static MusicCommand from(Music music) {
        return new MusicCommand(
                music.getSpotifyId(),
                music.getTitle(),
                music.getArtist(),
                music.getThumbnail(),
                music.getSpotifyUrl()
        );
    }
}
