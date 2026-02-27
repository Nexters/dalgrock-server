package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Record;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepository {

    Optional<Record> findById(Long id);

    Record save(Record record);

    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Record> findByUserIdAndWeeklyIdIn(Long userId, List<Long> weeklyIds);
}
