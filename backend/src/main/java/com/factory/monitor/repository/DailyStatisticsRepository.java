package com.factory.monitor.repository;

import com.factory.monitor.entity.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {

    /** 根据日期查询统计 */
    Optional<DailyStatistics> findByStatisticsDate(LocalDate statisticsDate);

    /** 查询日期范围内的统计 */
    List<DailyStatistics> findByStatisticsDateBetweenOrderByStatisticsDateAsc(LocalDate startDate, LocalDate endDate);

    /** 查询最近N天的统计 */
    @Query("SELECT d FROM DailyStatistics d ORDER BY d.statisticsDate DESC LIMIT :days")
    List<DailyStatistics> findRecentDays(@Param("days") int days);

    /** 查询某月的所有统计 */
    @Query("SELECT d FROM DailyStatistics d WHERE YEAR(d.statisticsDate) = :year AND MONTH(d.statisticsDate) = :month ORDER BY d.statisticsDate ASC")
    List<DailyStatistics> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
