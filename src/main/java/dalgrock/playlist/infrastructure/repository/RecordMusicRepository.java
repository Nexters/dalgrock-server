package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import java.util.List;

public interface RecordMusicRepository {

    List<GetRecordMusicDto> findAllByRecordId(Long id);
}
