package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.RecordMusic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRecordMusicRepository extends JpaRepository<RecordMusic, Long> {

    @Query(value = """
            select m.title, m.artist, m.thumbnail
            from record_music rm
            inner join musics m 
            on rm.music_id = m.id
            where rm.record_id = :recordId
            """, nativeQuery = true)
    List<GetRecordMusicDto> findAllByRecordId(@Param("recordId") Long recordId);
}
