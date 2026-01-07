package com.factory.monitor.repository;

import com.factory.monitor.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByWorkOrderNo(String workOrderNo);

    boolean existsByWorkOrderNo(String workOrderNo);

    List<WorkOrder> findBySalesOrderId(Long salesOrderId);

    List<WorkOrder> findByStatus(String status);

    List<WorkOrder> findByTeamId(Long teamId);

    @Query("SELECT w FROM WorkOrder w WHERE " +
           "(:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.productName LIKE %:keyword% OR w.orderNo LIKE %:keyword%) " +
           "AND (:status IS NULL OR w.status = :status) " +
           "AND (:teamId IS NULL OR w.teamId = :teamId) " +
           "AND (:startDate IS NULL OR w.planStartDate >= :startDate) " +
           "AND (:endDate IS NULL OR w.planEndDate <= :endDate) " +
           "ORDER BY w.createdAt DESC")
    Page<WorkOrder> findByCondition(@Param("keyword") String keyword,
                                    @Param("status") String status,
                                    @Param("teamId") Long teamId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    Pageable pageable);

    @Query("SELECT w FROM WorkOrder w WHERE w.teamId = :teamId AND w.status IN ('PENDING', 'IN_PROGRESS') ORDER BY w.planStartDate ASC")
    List<WorkOrder> findActiveByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.status = :status")
    Long countByStatus(@Param("status") String status);
}
