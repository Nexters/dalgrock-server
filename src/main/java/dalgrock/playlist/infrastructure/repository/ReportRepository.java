package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Report;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findByUserIdAndWeeklyId(Long userId, Long weeklyId);
}
