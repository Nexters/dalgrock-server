package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MusicRepository {

    Music save(Music music);

    void saveAll(List<Music> musics);

    boolean existsBySpotifyId(String spotifyId);

    Set<String> findExistingSpotifyIds(Set<String> spotifyIds);

    Optional<Music> findByArtistAndTitle(String artist, String title);

    List<Music> searchByKeyword(String keyword);
}
