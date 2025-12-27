package com.factory.monitor.repository;

import com.factory.monitor.entity.MonthlyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyStatisticsRepository extends JpaRepository<MonthlyStatistics, Long> {

    /** 根据年月查询统计 */
    Optional<MonthlyStatistics> findByYearAndMonth(Integer year, Integer month);

    /** 查询某年的所有月度统计 */
    List<MonthlyStatistics> findByYearOrderByMonthAsc(Integer year);

    /** 查询最近N个月的统计 */
    @Query("SELECT m FROM MonthlyStatistics m ORDER BY m.year DESC, m.month DESC LIMIT :months")
    List<MonthlyStatistics> findRecentMonths(@Param("months") int months);

    /** 查询年份范围内的统计 */
    @Query("SELECT m FROM MonthlyStatistics m WHERE m.year BETWEEN :startYear AND :endYear ORDER BY m.year ASC, m.month ASC")
    List<MonthlyStatistics> findByYearBetween(@Param("startYear") int startYear, @Param("endYear") int endYear);
}
