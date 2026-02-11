package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.Optional;

public interface MusicRepository {

    void saveAll(List<Music> musics);

    List<Music> searchByKeyword(String keyword);

    boolean existsBySpotifyId(String spotifyId);

    Optional<Music> findByArtistAndTitle(String artist, String title);

    Music save(Music music);
}
