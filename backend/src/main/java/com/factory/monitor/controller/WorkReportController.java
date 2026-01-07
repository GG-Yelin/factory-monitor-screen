package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.WorkOrderProcess;
import com.factory.monitor.entity.WorkReport;
import com.factory.monitor.security.UserPrincipal;
import com.factory.monitor.service.WorkReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work-reports")
public class WorkReportController {

    private final WorkReportService workReportService;

    public WorkReportController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    /**
     * 提交报工
     */
    @PostMapping
    public ApiResponse<WorkReport> submitReport(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @RequestBody WorkReport report) {
        try {
            // 设置操作工信息
            if (userPrincipal != null) {
                report.setOperatorId(userPrincipal.getUserId());
                // operatorName 应该从前端传入或从用户信息获取
            }

            WorkReport saved = workReportService.submitReport(report);
            return ApiResponse.success("报工成功", saved);
        } catch (Exception e) {
            log.error("报工失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询报工记录
     */
    @GetMapping
    public ApiResponse<Page<WorkReport>> listReports(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reportTime"));
        return ApiResponse.success(workReportService.findReports(operatorId, workOrderNo, startTime, endTime, pageable));
    }

    /**
     * 获取我的今日任务
     */
    @GetMapping("/my-tasks")
    public ApiResponse<List<WorkOrderProcess>> getMyTasks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }
        return ApiResponse.success(workReportService.findTodayTasks(userPrincipal.getUserId()));
    }

    /**
     * 获取我的报工记录
     */
    @GetMapping("/my-reports")
    public ApiResponse<List<WorkReport>> getMyReports(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }

        // 默认查询今天
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        return ApiResponse.success(workReportService.findReportsByOperator(userPrincipal.getUserId(), startTime, endTime));
    }
}
