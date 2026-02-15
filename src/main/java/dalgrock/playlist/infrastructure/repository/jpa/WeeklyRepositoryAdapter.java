package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.WeeklyRepository;
import dalgrock.playlist.model.Weekly;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeeklyRepositoryAdapter implements WeeklyRepository {

    private final JpaWeeklyRepository jpaWeeklyRepository;

    @Override
    public Optional<Weekly> findById(Long id) {
        return jpaWeeklyRepository.findById(id);
    }

    @Override
    public Optional<Weekly> findByYearAndMonthAndWeek(int year, int month, int week) {
        return jpaWeeklyRepository.findByYearAndMonthAndWeek(year, month, week);
    }

    @Override
    public List<Weekly> findByYearAndMonth(int year, int month) {
        return jpaWeeklyRepository.findByYearAndMonthOrderByWeekAsc(year, month);
    }

    @Override
    public Weekly save(Weekly weekly) {
        return jpaWeeklyRepository.save(weekly);
    }
}
