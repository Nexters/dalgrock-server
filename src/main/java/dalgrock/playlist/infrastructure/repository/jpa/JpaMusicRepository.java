package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Music;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findByArtistAndTitle(String artist, String title);
}
