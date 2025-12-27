package com.factory.monitor.service;

import com.factory.monitor.entity.*;
import com.factory.monitor.model.DashboardData;
import com.factory.monitor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ProductionRecordRepository productionRecordRepository;
    private final AlarmRecordRepository alarmRecordRepository;
    private final DeviceStatusLogRepository deviceStatusLogRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MonthlyStatisticsRepository monthlyStatisticsRepository;

    /**
     * 保存当前生产数据（定时调用）
     */
    @Transactional
    public void saveCurrentProductionData(DashboardData data) {
        if (data == null) {
            log.warn("Dashboard data is null, skip saving");
            return;
        }

        LocalDate today = LocalDate.now();

        // 保存或更新今日汇总记录
        ProductionRecord summaryRecord = productionRecordRepository
                .findByDeviceIdIsNullAndRecordDate(today)
                .orElse(ProductionRecord.builder()
                        .recordDate(today)
                        .deviceId(null)
                        .production(0)
                        .build());

        summaryRecord.setProduction(data.getTodayProduction() != null ? data.getTodayProduction() : 0);
        summaryRecord.setPlanQuantity(data.getPlanProduction() != null ? data.getPlanProduction() : 0);
        summaryRecord.setCompletionRate(data.getProductionRate());
        summaryRecord.setQualityRate(data.getQualityRate());

        productionRecordRepository.save(summaryRecord);

        // 更新今日统计
        updateDailyStatistics(today, data);

        log.debug("Saved production data for {}: production={}", today, data.getTodayProduction());
    }

    /**
     * 保存报警记录
     */
    @Transactional
    public void saveAlarmRecord(String deviceId, String deviceName, String alarmType,
                                String alarmContent, Integer level) {
        AlarmRecord alarm = AlarmRecord.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .alarmType(alarmType)
                .alarmContent(alarmContent)
                .level(level)
                .status(0)
                .alarmTime(LocalDateTime.now())
                .build();

        alarmRecordRepository.save(alarm);
        log.info("Saved alarm record: device={}, type={}", deviceName, alarmType);
    }

    /**
     * 保存设备状态日志
     */
    @Transactional
    public void saveDeviceStatusLog(String deviceId, String deviceName, Integer status) {
        // 检查是否与上次状态相同，避免重复记录
        Optional<DeviceStatusLog> lastLog = deviceStatusLogRepository.findTopByDeviceIdOrderByLogTimeDesc(deviceId);
        if (lastLog.isPresent() && lastLog.get().getStatus().equals(status)) {
            return; // 状态未变化，不记录
        }

        DeviceStatusLog statusLog = DeviceStatusLog.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .status(status)
                .logTime(LocalDateTime.now())
                .build();

        deviceStatusLogRepository.save(statusLog);
        log.debug("Saved device status log: device={}, status={}", deviceName, status);
    }

    /**
     * 更新每日统计
     */
    @Transactional
    public void updateDailyStatistics(LocalDate date, DashboardData data) {
        DailyStatistics stats = dailyStatisticsRepository.findByStatisticsDate(date)
                .orElse(DailyStatistics.builder()
                        .statisticsDate(date)
                        .totalProduction(0)
                        .build());

        stats.setTotalProduction(data.getTodayProduction() != null ? data.getTodayProduction() : 0);
        stats.setTotalPlan(data.getPlanProduction() != null ? data.getPlanProduction() : 0);
        stats.setCompletionRate(data.getProductionRate());
        stats.setQualityRate(data.getQualityRate());
        stats.setTotalDevices(data.getTotalDevices());
        stats.setOnlineDevices(data.getOnlineDevices());
        stats.setAvgOee(data.getEquipmentEfficiency());

        // 统计今日报警数
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        Integer alarmCount = alarmRecordRepository.countAlarmsBetween(startOfDay, endOfDay);
        stats.setAlarmCount(alarmCount);

        dailyStatisticsRepository.save(stats);
    }

    /**
     * 生成月度统计（每月1号凌晨执行，统计上月数据）
     */
    @Transactional
    public void generateMonthlyStatistics(int year, int month) {
        // 查询该月的所有日统计
        List<DailyStatistics> dailyStats = dailyStatisticsRepository.findByYearAndMonth(year, month);

        if (dailyStats.isEmpty()) {
            log.info("No daily statistics found for {}-{}", year, month);
            return;
        }

        // 计算月度汇总
        int totalProduction = dailyStats.stream()
                .mapToInt(d -> d.getTotalProduction() != null ? d.getTotalProduction() : 0)
                .sum();
        int totalPlan = dailyStats.stream()
                .mapToInt(d -> d.getTotalPlan() != null ? d.getTotalPlan() : 0)
                .sum();
        int qualifiedQuantity = dailyStats.stream()
                .mapToInt(d -> d.getQualifiedQuantity() != null ? d.getQualifiedQuantity() : 0)
                .sum();
        int totalAlarmCount = dailyStats.stream()
                .mapToInt(d -> d.getAlarmCount() != null ? d.getAlarmCount() : 0)
                .sum();

        double avgDailyProduction = dailyStats.stream()
                .mapToInt(d -> d.getTotalProduction() != null ? d.getTotalProduction() : 0)
                .average()
                .orElse(0);

        int maxDailyProduction = dailyStats.stream()
                .mapToInt(d -> d.getTotalProduction() != null ? d.getTotalProduction() : 0)
                .max()
                .orElse(0);

        int minDailyProduction = dailyStats.stream()
                .mapToInt(d -> d.getTotalProduction() != null ? d.getTotalProduction() : 0)
                .min()
                .orElse(0);

        double avgOee = dailyStats.stream()
                .filter(d -> d.getAvgOee() != null)
                .mapToDouble(DailyStatistics::getAvgOee)
                .average()
                .orElse(0);

        double completionRate = totalPlan > 0 ? (double) totalProduction / totalPlan * 100 : 0;
        double qualityRate = totalProduction > 0 ? (double) qualifiedQuantity / totalProduction * 100 : 98.5;

        // 保存或更新月度统计
        MonthlyStatistics monthlyStats = monthlyStatisticsRepository.findByYearAndMonth(year, month)
                .orElse(MonthlyStatistics.builder()
                        .year(year)
                        .month(month)
                        .build());

        monthlyStats.setTotalProduction(totalProduction);
        monthlyStats.setTotalPlan(totalPlan);
        monthlyStats.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        monthlyStats.setQualifiedQuantity(qualifiedQuantity);
        monthlyStats.setQualityRate(Math.round(qualityRate * 100.0) / 100.0);
        monthlyStats.setAvgDailyProduction(Math.round(avgDailyProduction * 100.0) / 100.0);
        monthlyStats.setMaxDailyProduction(maxDailyProduction);
        monthlyStats.setMinDailyProduction(minDailyProduction);
        monthlyStats.setTotalAlarmCount(totalAlarmCount);
        monthlyStats.setAvgOee(Math.round(avgOee * 100.0) / 100.0);
        monthlyStats.setWorkDays(dailyStats.size());

        monthlyStatisticsRepository.save(monthlyStats);
        log.info("Generated monthly statistics for {}-{}: totalProduction={}", year, month, totalProduction);
    }

    /**
     * 获取每日统计列表
     */
    public List<DailyStatistics> getDailyStatistics(LocalDate startDate, LocalDate endDate) {
        return dailyStatisticsRepository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(startDate, endDate);
    }

    /**
     * 获取最近N天的统计
     */
    public List<DailyStatistics> getRecentDailyStatistics(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return dailyStatisticsRepository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(startDate, endDate);
    }

    /**
     * 获取月度统计列表
     */
    public List<MonthlyStatistics> getMonthlyStatistics(int year) {
        return monthlyStatisticsRepository.findByYearOrderByMonthAsc(year);
    }

    /**
     * 获取最近N个月的统计
     */
    public List<MonthlyStatistics> getRecentMonthlyStatistics(int months) {
        return monthlyStatisticsRepository.findRecentMonths(months);
    }

    /**
     * 获取今日统计
     */
    public Optional<DailyStatistics> getTodayStatistics() {
        return dailyStatisticsRepository.findByStatisticsDate(LocalDate.now());
    }

    /**
     * 获取本月统计
     */
    public Optional<MonthlyStatistics> getCurrentMonthStatistics() {
        LocalDate now = LocalDate.now();
        return monthlyStatisticsRepository.findByYearAndMonth(now.getYear(), now.getMonthValue());
    }

    /**
     * 获取报警记录
     */
    public List<AlarmRecord> getRecentAlarms() {
        return alarmRecordRepository.findTop10ByOrderByAlarmTimeDesc();
    }

    /**
     * 处理报警
     */
    @Transactional
    public void handleAlarm(Long alarmId, String handler, String remark) {
        alarmRecordRepository.findById(alarmId).ifPresent(alarm -> {
            alarm.setStatus(1);
            alarm.setHandleTime(LocalDateTime.now());
            alarm.setHandler(handler);
            alarm.setHandleRemark(remark);
            alarmRecordRepository.save(alarm);
        });
    }
}
