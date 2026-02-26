package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Report;
import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(Long id);

    Optional<Report> findByUserIdAndWeeklyId(Long userId, Long weeklyId);

    List<Report> findByUserIdAndWeeklyIdIn(Long userId, List<Long> weeklyIds);
}
