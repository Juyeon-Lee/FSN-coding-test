package org.fsn.codingtest.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatisticRepository extends JpaRepository<Statistic,Long> {
    List<Statistic> findAllByDate(LocalDate date);
    Optional<Statistic> findByDateAndTime(LocalDate date, int time);
}
