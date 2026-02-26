package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.ReportRepository;
import dalgrock.playlist.model.Report;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final JpaReportRepository jpaReportRepository;

    @Override
    public Report save(Report report) {
        return jpaReportRepository.save(report);
    }

    @Override
    public Optional<Report> findByUserIdAndWeeklyId(Long userId, Long weeklyId) {
        return jpaReportRepository.findByUserIdAndWeekly_Id(userId, weeklyId);
    }

    @Override
    public List<Report> findByUserIdAndWeeklyIdIn(Long userId, List<Long> weeklyIds) {
        return jpaReportRepository.findByUserIdAndWeeklyIdIn(userId, weeklyIds);
    }
}
