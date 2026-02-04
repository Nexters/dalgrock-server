package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
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
}
