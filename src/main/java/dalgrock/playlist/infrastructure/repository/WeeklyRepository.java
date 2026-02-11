package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Weekly;
import java.util.Optional;

public interface WeeklyRepository {

    Optional<Weekly> findById(Long id);

    Optional<Weekly> findByYearAndMonthAndWeek(int year, int month, int week);

    Weekly save(Weekly weekly);
}
