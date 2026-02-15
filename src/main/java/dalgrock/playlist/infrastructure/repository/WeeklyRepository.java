package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.Weekly;
import java.util.List;
import java.util.Optional;

public interface WeeklyRepository {

    Optional<Weekly> findById(Long id);

    Optional<Weekly> findByYearAndMonthAndWeek(int year, int month, int week);

    List<Weekly> findByYearAndMonth(int year, int month);

    Weekly save(Weekly weekly);
}
