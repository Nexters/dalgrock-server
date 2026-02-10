package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.Weekly;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWeeklyRepository extends JpaRepository<Weekly, Long> {

    Optional<Weekly> findByYearAndMonthAndWeek(int year, int month, int week);
}
