package com.factory.monitor.repository;

import com.factory.monitor.entity.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, Long> {

    /** 根据日期查询所有记录 */
    List<ProductionRecord> findByRecordDate(LocalDate recordDate);

    /** 根据设备ID和日期查询 */
    Optional<ProductionRecord> findByDeviceIdAndRecordDate(String deviceId, LocalDate recordDate);

    /** 查询汇总记录（deviceId为null的记录） */
    Optional<ProductionRecord> findByDeviceIdIsNullAndRecordDate(LocalDate recordDate);

    /** 查询日期范围内的记录 */
    List<ProductionRecord> findByRecordDateBetweenOrderByRecordDateAsc(LocalDate startDate, LocalDate endDate);

    /** 查询日期范围内的汇总记录 */
    @Query("SELECT p FROM ProductionRecord p WHERE p.deviceId IS NULL AND p.recordDate BETWEEN :startDate AND :endDate ORDER BY p.recordDate ASC")
    List<ProductionRecord> findSummaryRecordsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 统计某日期范围内的总产量 */
    @Query("SELECT COALESCE(SUM(p.production), 0) FROM ProductionRecord p WHERE p.deviceId IS NULL AND p.recordDate BETWEEN :startDate AND :endDate")
    Integer sumProductionBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 查询某月的所有汇总记录 */
    @Query("SELECT p FROM ProductionRecord p WHERE p.deviceId IS NULL AND YEAR(p.recordDate) = :year AND MONTH(p.recordDate) = :month ORDER BY p.recordDate ASC")
    List<ProductionRecord> findMonthlyRecords(@Param("year") int year, @Param("month") int month);
}
