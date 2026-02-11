package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Record;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRecordRepository extends JpaRepository<Record, Long> {

    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM records r JOIN FETCH r.weekly WHERE r.userId = :userId AND r.weekly.id IN :weeklyIds")
    List<Record> findByUserIdAndWeeklyIdInFetchWeekly(@Param("userId") Long userId, @Param("weeklyIds") List<Long> weeklyIds);
}
