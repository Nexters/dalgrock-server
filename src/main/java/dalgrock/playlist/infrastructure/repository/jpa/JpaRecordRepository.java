package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Record;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRecordRepository extends JpaRepository<Record, Long> {

    boolean existsByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(Long userId, LocalDateTime start, LocalDateTime end);

    boolean existsByUserIdAndRecordDateAndDeletedAtIsNull(Long userId, LocalDate recordDate);

    boolean existsByUserIdAndRecordDateIsNullAndCreatedAtBetweenAndDeletedAtIsNull(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM records r JOIN FETCH r.weekly WHERE r.userId = :userId AND r.weekly.id IN :weeklyIds AND r.deletedAt IS NULL")
    List<Record> findByUserIdAndWeeklyIdInFetchWeekly(@Param("userId") Long userId, @Param("weeklyIds") List<Long> weeklyIds);

    java.util.Optional<Record> findByIdAndDeletedAtIsNull(Long id);
}
