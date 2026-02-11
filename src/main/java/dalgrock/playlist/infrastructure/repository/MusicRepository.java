package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Music;
import java.util.Optional;

public interface MusicRepository {

    Optional<Music> findByArtistAndTitle(String artist, String title);

    Music save(Music music);
}
