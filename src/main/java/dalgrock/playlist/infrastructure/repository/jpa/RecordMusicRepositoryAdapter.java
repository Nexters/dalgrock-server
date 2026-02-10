package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.RecordMusic;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordMusicRepositoryAdapter implements RecordMusicRepository {

    private final JpaRecordMusicRepository jpaRecordMusicRepository;

    @Override
    public List<GetRecordMusicDto> findAllByRecordId(Long id) {
        return jpaRecordMusicRepository.findAllByRecordId(id);
    }

    @Override
    public RecordMusic save(RecordMusic recordMusic) {
        return jpaRecordMusicRepository.save(recordMusic);
    }
}
