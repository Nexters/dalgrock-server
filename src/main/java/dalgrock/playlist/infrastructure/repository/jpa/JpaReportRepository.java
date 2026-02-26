package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserIdAndWeekly_Id(Long userId, Long weeklyId);
}
