package com.factory.monitor.controller;

import com.factory.monitor.entity.AlarmRecord;
import com.factory.monitor.entity.DailyStatistics;
import com.factory.monitor.entity.MonthlyStatistics;
import com.factory.monitor.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取每日统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyStatistics>> getDailyStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statisticsService.getDailyStatistics(startDate, endDate));
    }

    /**
     * 获取最近N天的统计
     */
    @GetMapping("/daily/recent")
    public ResponseEntity<List<DailyStatistics>> getRecentDailyStatistics(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(statisticsService.getRecentDailyStatistics(days));
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/daily/today")
    public ResponseEntity<DailyStatistics> getTodayStatistics() {
        return ResponseEntity.ok(
                statisticsService.getTodayStatistics().orElse(null)
        );
    }

    /**
     * 获取某年的月度统计
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatistics>> getMonthlyStatistics(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return ResponseEntity.ok(statisticsService.getMonthlyStatistics(year));
    }

    /**
     * 获取最近N个月的统计
     */
    @GetMapping("/monthly/recent")
    public ResponseEntity<List<MonthlyStatistics>> getRecentMonthlyStatistics(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(statisticsService.getRecentMonthlyStatistics(months));
    }

    /**
     * 获取本月统计
     */
    @GetMapping("/monthly/current")
    public ResponseEntity<MonthlyStatistics> getCurrentMonthStatistics() {
        return ResponseEntity.ok(
                statisticsService.getCurrentMonthStatistics().orElse(null)
        );
    }

    /**
     * 获取统计概览（今日 + 本月）
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getStatisticsOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 今日统计
        DailyStatistics today = statisticsService.getTodayStatistics().orElse(null);
        overview.put("today", today);

        // 本月统计
        MonthlyStatistics currentMonth = statisticsService.getCurrentMonthStatistics().orElse(null);
        overview.put("currentMonth", currentMonth);

        // 最近7天趋势
        List<DailyStatistics> weekTrend = statisticsService.getRecentDailyStatistics(7);
        overview.put("weekTrend", weekTrend);

        // 最近12个月趋势
        List<MonthlyStatistics> yearTrend = statisticsService.getRecentMonthlyStatistics(12);
        overview.put("yearTrend", yearTrend);

        return ResponseEntity.ok(overview);
    }

    /**
     * 获取报警记录
     */
    @GetMapping("/alarms")
    public ResponseEntity<List<AlarmRecord>> getRecentAlarms() {
        return ResponseEntity.ok(statisticsService.getRecentAlarms());
    }

    /**
     * 处理报警
     */
    @PostMapping("/alarms/{id}/handle")
    public ResponseEntity<Void> handleAlarm(
            @PathVariable Long id,
            @RequestParam(required = false) String handler,
            @RequestParam(required = false) String remark) {
        statisticsService.handleAlarm(id, handler, remark);
        return ResponseEntity.ok().build();
    }

    /**
     * 手动触发月度统计生成（用于补录数据）
     */
    @PostMapping("/monthly/generate")
    public ResponseEntity<Void> generateMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        statisticsService.generateMonthlyStatistics(year, month);
        return ResponseEntity.ok().build();
    }
}
