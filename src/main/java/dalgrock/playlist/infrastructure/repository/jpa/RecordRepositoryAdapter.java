package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.model.Record;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordRepositoryAdapter implements RecordRepository {

    private final JpaRecordRepository jpaRecordRepository;

    @Override
    public Optional<Record> findById(Long id) {
        return jpaRecordRepository.findById(id);
    }

    @Override
    public Record save(Record record) {
        return jpaRecordRepository.save(record);
    }
}
