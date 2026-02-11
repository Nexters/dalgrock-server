package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Music;
import java.util.List;

public interface MusicRepository {

    void saveAll(List<Music> musics);

    List<Music> searchByKeyword(String keyword);

    boolean existsBySpotifyId(String spotifyId);
}
