package com.factory.monitor.repository;

import com.factory.monitor.entity.WorkOrderProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderProcessRepository extends JpaRepository<WorkOrderProcess, Long> {

    List<WorkOrderProcess> findByWorkOrderIdOrderBySortOrder(Long workOrderId);

    List<WorkOrderProcess> findByOperatorId(Long operatorId);

    List<WorkOrderProcess> findByOperatorIdAndStatus(Long operatorId, String status);

    Optional<WorkOrderProcess> findByQrCode(String qrCode);

    @Query("SELECT p FROM WorkOrderProcess p WHERE p.operatorId = :operatorId AND p.status IN ('PENDING', 'IN_PROGRESS') ORDER BY p.createdAt DESC")
    List<WorkOrderProcess> findActiveByOperatorId(@Param("operatorId") Long operatorId);

    @Query("SELECT p FROM WorkOrderProcess p WHERE p.needInspection = 1 AND p.inspectionStatus = 'PENDING' ORDER BY p.createdAt ASC")
    List<WorkOrderProcess> findPendingInspection();

    @Query("SELECT p FROM WorkOrderProcess p WHERE p.needInspection = 1 AND p.inspectorId = :inspectorId AND p.inspectionStatus = 'PENDING'")
    List<WorkOrderProcess> findPendingInspectionByInspector(@Param("inspectorId") Long inspectorId);

    void deleteByWorkOrderId(Long workOrderId);
}
