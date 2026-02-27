package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.model.Record;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordRepositoryAdapter implements RecordRepository {

    private final JpaRecordRepository jpaRecordRepository;

    @Override
    public Optional<Record> findById(Long id) {
        return jpaRecordRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Record save(Record record) {
        return jpaRecordRepository.save(record);
    }

    @Override
    public boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return jpaRecordRepository.existsByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId, start, end);
    }

    @Override
    public List<Record> findByUserIdAndWeeklyIdIn(Long userId, List<Long> weeklyIds) {
        return jpaRecordRepository.findByUserIdAndWeeklyIdInFetchWeekly(userId, weeklyIds);
    }
}
