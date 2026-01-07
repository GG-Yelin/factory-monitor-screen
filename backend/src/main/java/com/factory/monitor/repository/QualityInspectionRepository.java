package com.factory.monitor.repository;

import com.factory.monitor.entity.QualityInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {

    List<QualityInspection> findByWorkOrderId(Long workOrderId);

    List<QualityInspection> findByWorkOrderProcessId(Long workOrderProcessId);

    List<QualityInspection> findByInspectorId(Long inspectorId);

    @Query("SELECT q FROM QualityInspection q WHERE " +
           "(:inspectorId IS NULL OR q.inspectorId = :inspectorId) " +
           "AND (:workOrderNo IS NULL OR q.workOrderNo LIKE %:workOrderNo%) " +
           "AND (:result IS NULL OR q.result = :result) " +
           "AND (:startTime IS NULL OR q.inspectionTime >= :startTime) " +
           "AND (:endTime IS NULL OR q.inspectionTime <= :endTime) " +
           "ORDER BY q.inspectionTime DESC")
    Page<QualityInspection> findByCondition(@Param("inspectorId") Long inspectorId,
                                            @Param("workOrderNo") String workOrderNo,
                                            @Param("result") String result,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            Pageable pageable);

    @Query("SELECT SUM(q.qualifiedQuantity) FROM QualityInspection q WHERE q.workOrderId = :workOrderId")
    Integer sumQualifiedQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);

    @Query("SELECT SUM(q.unqualifiedQuantity) FROM QualityInspection q WHERE q.workOrderId = :workOrderId")
    Integer sumUnqualifiedQuantityByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
