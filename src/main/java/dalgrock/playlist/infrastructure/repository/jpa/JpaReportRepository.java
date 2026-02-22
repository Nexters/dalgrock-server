package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReportRepository extends JpaRepository<Report, Long> {
}
