package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaMusicRepository extends JpaRepository<Music, Long> {

    @Query("""
            SELECT m
            FROM musics m
            WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.artist) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Music> searchByKeyword(@Param("keyword") String keyword);

    Optional<Music> findByArtistAndTitle(String artist, String title);
}
