package com.factory.monitor.service;

import com.factory.monitor.entity.*;
import com.factory.monitor.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;
    private final ProductRepository productRepository;
    private final ProcessStepRepository processStepRepository;
    private final TeamRepository teamRepository;

    public OrderService(SalesOrderRepository salesOrderRepository,
                        WorkOrderRepository workOrderRepository,
                        WorkOrderProcessRepository workOrderProcessRepository,
                        ProductRepository productRepository,
                        ProcessStepRepository processStepRepository,
                        TeamRepository teamRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderProcessRepository = workOrderProcessRepository;
        this.productRepository = productRepository;
        this.processStepRepository = processStepRepository;
        this.teamRepository = teamRepository;
    }

    // ========== 销售订单管理 ==========

    public SalesOrder findSalesOrderById(Long id) {
        return salesOrderRepository.findById(id).orElse(null);
    }

    public Page<SalesOrder> findSalesOrders(String keyword, String status, LocalDate startDate,
                                            LocalDate endDate, Pageable pageable) {
        return salesOrderRepository.findByCondition(keyword, status, startDate, endDate, pageable);
    }

    public List<SalesOrder> findPendingOrders() {
        return salesOrderRepository.findByStatusIn(List.of("DRAFT", "PENDING"));
    }

    @Transactional
    public SalesOrder createSalesOrder(SalesOrder order) {
        // 生成订单号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);

        // 获取产品信息
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        order.setProductName(product.getName());
        order.setProductCode(product.getCode());

        if (order.getStatus() == null) {
            order.setStatus("DRAFT");
        }

        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder updateSalesOrder(SalesOrder order) {
        SalesOrder existing = salesOrderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 如果产品变更，更新产品信息
        if (!existing.getProductId().equals(order.getProductId())) {
            Product product = productRepository.findById(order.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            existing.setProductId(order.getProductId());
            existing.setProductName(product.getName());
            existing.setProductCode(product.getCode());
        }

        existing.setQuantity(order.getQuantity());
        existing.setDeliveryDate(order.getDeliveryDate());
        existing.setCustomerName(order.getCustomerName());
        existing.setPriority(order.getPriority());
        existing.setRemark(order.getRemark());

        return salesOrderRepository.save(existing);
    }

    @Transactional
    public void updateSalesOrderStatus(Long id, String status) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        order.setStatus(status);
        salesOrderRepository.save(order);
    }

    @Transactional
    public void deleteSalesOrder(Long id) {
        salesOrderRepository.deleteById(id);
    }

    // ========== 派工单管理 ==========

    public WorkOrder findWorkOrderById(Long id) {
        return workOrderRepository.findById(id).orElse(null);
    }

    public WorkOrder findWorkOrderByNo(String workOrderNo) {
        return workOrderRepository.findByWorkOrderNo(workOrderNo).orElse(null);
    }

    public Page<WorkOrder> findWorkOrders(String keyword, String status, Long teamId,
                                          LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return workOrderRepository.findByCondition(keyword, status, teamId, startDate, endDate, pageable);
    }

    public List<WorkOrder> findWorkOrdersBySalesOrderId(Long salesOrderId) {
        return workOrderRepository.findBySalesOrderId(salesOrderId);
    }

    @Transactional
    public WorkOrder createWorkOrder(Long salesOrderId, Long teamId, LocalDate planStartDate, LocalDate planEndDate) {
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new RuntimeException("销售订单不存在"));

        Product product = productRepository.findById(salesOrder.getProductId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        if (product.getTemplateId() == null) {
            throw new RuntimeException("产品未绑定工序模板");
        }

        // 生成派工单号
        String workOrderNo = generateWorkOrderNo();

        // 创建派工单
        WorkOrder workOrder = WorkOrder.builder()
                .workOrderNo(workOrderNo)
                .salesOrderId(salesOrderId)
                .orderNo(salesOrder.getOrderNo())
                .productId(product.getId())
                .productName(product.getName())
                .productCode(product.getCode())
                .planQuantity(salesOrder.getQuantity())
                .teamId(teamId)
                .planStartDate(planStartDate)
                .planEndDate(planEndDate)
                .qrCode("WO:" + workOrderNo)
                .status("PENDING")
                .build();

        // 设置班组名称
        if (teamId != null) {
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team != null) {
                workOrder.setTeamName(team.getName());
            }
        }

        workOrder = workOrderRepository.save(workOrder);

        // 创建派工单工序
        List<ProcessStep> steps = processStepRepository.findByTemplateIdOrderBySortOrder(product.getTemplateId());
        for (ProcessStep step : steps) {
            WorkOrderProcess process = WorkOrderProcess.builder()
                    .workOrderId(workOrder.getId())
                    .workOrderNo(workOrderNo)
                    .processStepId(step.getId())
                    .processName(step.getName())
                    .sortOrder(step.getSortOrder())
                    .planQuantity(salesOrder.getQuantity())
                    .unitPrice(step.getUnitPrice())
                    .needInspection(step.getNeedInspection())
                    .qrCode("WO:" + workOrderNo + ":P:" + step.getId())
                    .status("PENDING")
                    .build();
            workOrderProcessRepository.save(process);
        }

        // 更新销售订单状态
        salesOrder.setStatus("SCHEDULED");
        salesOrderRepository.save(salesOrder);

        return workOrder;
    }

    @Transactional
    public void updateWorkOrderStatus(Long id, String status) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("派工单不存在"));

        workOrder.setStatus(status);

        if ("IN_PROGRESS".equals(status) && workOrder.getActualStartTime() == null) {
            workOrder.setActualStartTime(LocalDateTime.now());
        }
        if ("COMPLETED".equals(status)) {
            workOrder.setActualEndTime(LocalDateTime.now());
        }

        workOrderRepository.save(workOrder);

        // 更新关联的销售订单状态
        SalesOrder salesOrder = salesOrderRepository.findById(workOrder.getSalesOrderId()).orElse(null);
        if (salesOrder != null) {
            if ("IN_PROGRESS".equals(status)) {
                salesOrder.setStatus("IN_PROGRESS");
            } else if ("COMPLETED".equals(status)) {
                // 检查是否所有派工单都已完成
                List<WorkOrder> allWorkOrders = workOrderRepository.findBySalesOrderId(workOrder.getSalesOrderId());
                boolean allCompleted = allWorkOrders.stream()
                        .allMatch(wo -> "COMPLETED".equals(wo.getStatus()));
                if (allCompleted) {
                    salesOrder.setStatus("COMPLETED");
                }
            }
            salesOrderRepository.save(salesOrder);
        }
    }

    @Transactional
    public void deleteWorkOrder(Long id) {
        workOrderProcessRepository.deleteByWorkOrderId(id);
        workOrderRepository.deleteById(id);
    }

    // ========== 派工单工序管理 ==========

    public List<WorkOrderProcess> findProcessesByWorkOrderId(Long workOrderId) {
        return workOrderProcessRepository.findByWorkOrderIdOrderBySortOrder(workOrderId);
    }

    public WorkOrderProcess findProcessById(Long id) {
        return workOrderProcessRepository.findById(id).orElse(null);
    }

    public WorkOrderProcess findProcessByQrCode(String qrCode) {
        return workOrderProcessRepository.findByQrCode(qrCode).orElse(null);
    }

    @Transactional
    public void assignOperator(Long processId, Long operatorId, String operatorName) {
        WorkOrderProcess process = workOrderProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("工序不存在"));
        process.setOperatorId(operatorId);
        process.setOperatorName(operatorName);
        workOrderProcessRepository.save(process);
    }

    // ========== 辅助方法 ==========

    private String generateOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "SO" + date + random;
    }

    private String generateWorkOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "WO" + date + random;
    }
}
