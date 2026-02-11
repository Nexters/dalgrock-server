package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Record;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRecordRepository extends JpaRepository<Record, Long> {

    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
