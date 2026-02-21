package dalgrock.playlist.infrastructure.spotify.dto;

import dalgrock.playlist.model.Music;

public record MusicCommand(
        String spotifyId,
        String title,
        String artist,
        String thumbnail,
        String spotifyUrl,
        String genre
) {

    public static MusicCommand from(Music music) {
        return new MusicCommand(
                null,
                music.getTitle(),
                music.getArtist(),
                music.getThumbnail(),
                null,
                music.getGenre()
        );
    }
}
