package com.factory.monitor.service;

import com.factory.monitor.entity.*;
import com.factory.monitor.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class WorkReportService {

    private final WorkReportRepository workReportRepository;
    private final WorkOrderProcessRepository workOrderProcessRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WageRecordRepository wageRecordRepository;
    private final UserRepository userRepository;

    public WorkReportService(WorkReportRepository workReportRepository,
                             WorkOrderProcessRepository workOrderProcessRepository,
                             WorkOrderRepository workOrderRepository,
                             WageRecordRepository wageRecordRepository,
                             UserRepository userRepository) {
        this.workReportRepository = workReportRepository;
        this.workOrderProcessRepository = workOrderProcessRepository;
        this.workOrderRepository = workOrderRepository;
        this.wageRecordRepository = wageRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 提交报工
     */
    @Transactional
    public WorkReport submitReport(WorkReport report) {
        // 获取工序信息
        WorkOrderProcess process = workOrderProcessRepository.findById(report.getWorkOrderProcessId())
                .orElseThrow(() -> new RuntimeException("工序不存在"));

        // 获取派工单信息
        WorkOrder workOrder = workOrderRepository.findById(process.getWorkOrderId())
                .orElseThrow(() -> new RuntimeException("派工单不存在"));

        // 校验：完成数量+报废数量不能大于待完成数量
        int remaining = process.getPlanQuantity() - process.getCompletedQuantity() - process.getScrapQuantity();
        if (report.getCompletedQuantity() + report.getScrapQuantity() > remaining) {
            throw new RuntimeException("报工数量超过待完成数量");
        }

        // 设置报工记录信息
        report.setWorkOrderId(workOrder.getId());
        report.setWorkOrderNo(workOrder.getWorkOrderNo());
        report.setProcessName(process.getProcessName());
        report.setUnitPrice(process.getUnitPrice());

        // 计算工资
        if (process.getUnitPrice() != null) {
            BigDecimal wage = process.getUnitPrice().multiply(BigDecimal.valueOf(report.getCompletedQuantity()));
            report.setWage(wage);
        }

        if (report.getReportTime() == null) {
            report.setReportTime(LocalDateTime.now());
        }

        // 保存报工记录
        report = workReportRepository.save(report);

        // 更新工序进度
        process.setCompletedQuantity(process.getCompletedQuantity() + report.getCompletedQuantity());
        process.setQualifiedQuantity(process.getQualifiedQuantity() + report.getCompletedQuantity());
        process.setScrapQuantity(process.getScrapQuantity() + report.getScrapQuantity());
        process.setTotalMinutes(process.getTotalMinutes() + report.getWorkMinutes());

        // 更新工序状态
        if (process.getActualStartTime() == null) {
            process.setActualStartTime(LocalDateTime.now());
            process.setStatus("IN_PROGRESS");
        }
        if (process.getCompletedQuantity() + process.getScrapQuantity() >= process.getPlanQuantity()) {
            process.setStatus("COMPLETED");
            process.setActualEndTime(LocalDateTime.now());
        }

        workOrderProcessRepository.save(process);

        // 更新派工单进度
        updateWorkOrderProgress(workOrder.getId());

        // 生成工资记录
        createWageRecord(report);

        return report;
    }

    /**
     * 更新派工单进度
     */
    private void updateWorkOrderProgress(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) return;

        List<WorkOrderProcess> processes = workOrderProcessRepository.findByWorkOrderIdOrderBySortOrder(workOrderId);

        // 计算完成数量（取最后一道工序的完成数量）
        int completedQty = 0;
        int qualifiedQty = 0;
        int scrapQty = 0;
        boolean allCompleted = true;
        boolean anyStarted = false;

        for (WorkOrderProcess p : processes) {
            if ("COMPLETED".equals(p.getStatus())) {
                completedQty = Math.max(completedQty, p.getCompletedQuantity());
            } else {
                allCompleted = false;
            }
            if (!"PENDING".equals(p.getStatus())) {
                anyStarted = true;
            }
            qualifiedQty = Math.max(qualifiedQty, p.getQualifiedQuantity());
            scrapQty += p.getScrapQuantity();
        }

        workOrder.setCompletedQuantity(completedQty);
        workOrder.setQualifiedQuantity(qualifiedQty);
        workOrder.setScrapQuantity(scrapQty);

        // 更新状态
        if (allCompleted && !processes.isEmpty()) {
            workOrder.setStatus("COMPLETED");
            if (workOrder.getActualEndTime() == null) {
                workOrder.setActualEndTime(LocalDateTime.now());
            }
        } else if (anyStarted) {
            workOrder.setStatus("IN_PROGRESS");
            if (workOrder.getActualStartTime() == null) {
                workOrder.setActualStartTime(LocalDateTime.now());
            }
        }

        workOrderRepository.save(workOrder);
    }

    /**
     * 生成工资记录
     */
    private void createWageRecord(WorkReport report) {
        if (report.getWage() == null || report.getWage().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        User user = userRepository.findById(report.getOperatorId()).orElse(null);

        WorkOrder workOrder = workOrderRepository.findById(report.getWorkOrderId()).orElse(null);

        WageRecord wage = WageRecord.builder()
                .userId(report.getOperatorId())
                .userName(report.getOperatorName())
                .employeeNo(user != null ? user.getEmployeeNo() : null)
                .workDate(report.getReportTime().toLocalDate())
                .workReportId(report.getId())
                .workOrderNo(report.getWorkOrderNo())
                .productName(workOrder != null ? workOrder.getProductName() : null)
                .processName(report.getProcessName())
                .quantity(report.getCompletedQuantity())
                .unitPrice(report.getUnitPrice())
                .wage(report.getWage())
                .wageType("PIECE")
                .status("PENDING")
                .build();

        wageRecordRepository.save(wage);
    }

    /**
     * 获取报工记录
     */
    public Page<WorkReport> findReports(Long operatorId, String workOrderNo,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        Pageable pageable) {
        return workReportRepository.findByCondition(operatorId, workOrderNo, startTime, endTime, pageable);
    }

    /**
     * 获取员工今日任务
     */
    public List<WorkOrderProcess> findTodayTasks(Long operatorId) {
        return workOrderProcessRepository.findActiveByOperatorId(operatorId);
    }

    /**
     * 获取员工报工记录
     */
    public List<WorkReport> findReportsByOperator(Long operatorId, LocalDateTime startTime, LocalDateTime endTime) {
        return workReportRepository.findByOperatorIdAndTimeRange(operatorId, startTime, endTime);
    }
}
