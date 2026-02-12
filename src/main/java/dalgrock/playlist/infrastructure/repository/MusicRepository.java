package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.Optional;

public interface MusicRepository {

    Music save(Music music);

    void saveAll(List<Music> musics);

    Optional<Music> findByArtistAndTitle(String artist, String title);

    List<Music> searchByKeyword(String keyword);
}
