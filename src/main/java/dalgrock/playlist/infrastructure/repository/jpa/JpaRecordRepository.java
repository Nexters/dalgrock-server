package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRecordRepository extends JpaRepository<Record, Long> {
}
