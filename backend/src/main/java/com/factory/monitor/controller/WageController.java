package com.factory.monitor.controller;

import com.factory.monitor.dto.ApiResponse;
import com.factory.monitor.entity.WageRecord;
import com.factory.monitor.repository.WageRecordRepository;
import com.factory.monitor.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 工资记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/wages")
public class WageController {

    private final WageRecordRepository wageRecordRepository;

    public WageController(WageRecordRepository wageRecordRepository) {
        this.wageRecordRepository = wageRecordRepository;
    }

    /**
     * 获取我的工资记录
     */
    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyWages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }

        // 默认查询本月
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Long userId = userPrincipal.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        // 查询工资记录
        Page<WageRecord> wages = wageRecordRepository.findByCondition(userId, startDate, endDate, null, pageable);

        // 计算汇总
        BigDecimal totalWage = wageRecordRepository.sumWageByUserIdAndDateRange(userId, startDate, endDate);

        // 返回 Page 格式的数据，前端期望 content, last, totalElements 等字段
        Map<String, Object> result = new HashMap<>();
        result.put("content", wages.getContent());
        result.put("totalElements", wages.getTotalElements());
        result.put("totalPages", wages.getTotalPages());
        result.put("last", wages.isLast());
        result.put("first", wages.isFirst());
        result.put("number", wages.getNumber());
        result.put("size", wages.getSize());
        result.put("summary", Map.of(
            "totalAmount", totalWage != null ? totalWage : BigDecimal.ZERO,
            "totalQuantity", wages.getContent().stream().mapToInt(WageRecord::getQuantity).sum()
        ));

        return ApiResponse.success(result);
    }

    /**
     * 获取工资汇总
     */
    @GetMapping("/my/summary")
    public ApiResponse<Map<String, Object>> getMyWageSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录");
        }

        Long userId = userPrincipal.getUserId();

        // 本月数据
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now();
        BigDecimal monthWage = wageRecordRepository.sumWageByUserIdAndDateRange(userId, monthStart, monthEnd);

        // 今日数据
        LocalDate today = LocalDate.now();
        BigDecimal todayWage = wageRecordRepository.sumWageByUserIdAndDateRange(userId, today, today);

        // 自定义范围
        BigDecimal rangeWage = BigDecimal.ZERO;
        if (startDate != null && endDate != null) {
            rangeWage = wageRecordRepository.sumWageByUserIdAndDateRange(userId, startDate, endDate);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayWage", todayWage != null ? todayWage : BigDecimal.ZERO);
        result.put("monthWage", monthWage != null ? monthWage : BigDecimal.ZERO);
        result.put("rangeWage", rangeWage != null ? rangeWage : BigDecimal.ZERO);

        return ApiResponse.success(result);
    }
}
