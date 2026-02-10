package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.RecordMusic;
import java.util.List;

public interface RecordMusicRepository {

    List<GetRecordMusicDto> findAllByRecordId(Long id);

    RecordMusic save(RecordMusic recordMusic);
}
