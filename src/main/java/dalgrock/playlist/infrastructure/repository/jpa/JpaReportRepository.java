package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserIdAndWeekly_Id(Long userId, Long weeklyId);

    @Query("SELECT r FROM reports r JOIN FETCH r.weekly WHERE r.userId = :userId AND r.weekly.id IN :weeklyIds")
    List<Report> findByUserIdAndWeeklyIdIn(@Param("userId") Long userId, @Param("weeklyIds") List<Long> weeklyIds);
}
