package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final JpaReportRepository jpaReportRepository;
}
