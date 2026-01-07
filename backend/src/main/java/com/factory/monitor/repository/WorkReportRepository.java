package com.factory.monitor.repository;

import com.factory.monitor.entity.WorkReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkReportRepository extends JpaRepository<WorkReport, Long> {

    List<WorkReport> findByWorkOrderProcessId(Long workOrderProcessId);

    List<WorkReport> findByWorkOrderId(Long workOrderId);

    List<WorkReport> findByOperatorId(Long operatorId);

    @Query("SELECT r FROM WorkReport r WHERE r.operatorId = :operatorId AND r.reportTime >= :startTime AND r.reportTime <= :endTime ORDER BY r.reportTime DESC")
    List<WorkReport> findByOperatorIdAndTimeRange(@Param("operatorId") Long operatorId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT r FROM WorkReport r WHERE " +
           "(:operatorId IS NULL OR r.operatorId = :operatorId) " +
           "AND (:workOrderNo IS NULL OR r.workOrderNo LIKE %:workOrderNo%) " +
           "AND (:startTime IS NULL OR r.reportTime >= :startTime) " +
           "AND (:endTime IS NULL OR r.reportTime <= :endTime) " +
           "ORDER BY r.reportTime DESC")
    Page<WorkReport> findByCondition(@Param("operatorId") Long operatorId,
                                     @Param("workOrderNo") String workOrderNo,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);

    @Query("SELECT SUM(r.completedQuantity) FROM WorkReport r WHERE r.workOrderProcessId = :processId")
    Integer sumCompletedQuantityByProcessId(@Param("processId") Long processId);

    @Query("SELECT SUM(r.scrapQuantity) FROM WorkReport r WHERE r.workOrderProcessId = :processId")
    Integer sumScrapQuantityByProcessId(@Param("processId") Long processId);
}
