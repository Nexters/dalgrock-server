package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Record;
import java.util.Optional;

public interface RecordRepository {

    Optional<Record> findById(Long id);
}
