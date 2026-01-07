package com.factory.monitor.repository;

import com.factory.monitor.entity.WageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WageRecordRepository extends JpaRepository<WageRecord, Long> {

    List<WageRecord> findByUserId(Long userId);

    List<WageRecord> findByUserIdAndWorkDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT w FROM WageRecord w WHERE " +
           "(:userId IS NULL OR w.userId = :userId) " +
           "AND (:startDate IS NULL OR w.workDate >= :startDate) " +
           "AND (:endDate IS NULL OR w.workDate <= :endDate) " +
           "AND (:status IS NULL OR w.status = :status) " +
           "ORDER BY w.workDate DESC")
    Page<WageRecord> findByCondition(@Param("userId") Long userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("status") String status,
                                     Pageable pageable);

    @Query("SELECT SUM(w.wage) FROM WageRecord w WHERE w.userId = :userId AND w.workDate >= :startDate AND w.workDate <= :endDate")
    BigDecimal sumWageByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT w.userId, w.userName, SUM(w.wage) as totalWage FROM WageRecord w " +
           "WHERE w.workDate >= :startDate AND w.workDate <= :endDate " +
           "GROUP BY w.userId, w.userName " +
           "ORDER BY totalWage DESC")
    List<Object[]> sumWageGroupByUser(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}
