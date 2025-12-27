package com.factory.monitor.task;

import com.factory.monitor.model.DashboardData;
import com.factory.monitor.model.DeviceInfo;
import com.factory.monitor.service.DashboardService;
import com.factory.monitor.service.StatisticsService;
import com.factory.monitor.websocket.MonitorWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class DataPollingTask {

    private final MonitorWebSocketHandler webSocketHandler;
    private final DashboardService dashboardService;
    private final StatisticsService statisticsService;

    @Value("${polling.interval:3000}")
    private long pollingInterval;

    private LocalDate lastSaveDate = null;

    public DataPollingTask(MonitorWebSocketHandler webSocketHandler,
                           DashboardService dashboardService,
                           StatisticsService statisticsService) {
        this.webSocketHandler = webSocketHandler;
        this.dashboardService = dashboardService;
        this.statisticsService = statisticsService;
    }

    /**
     * 定时轮询数据并推送
     */
    @Scheduled(fixedDelayString = "${polling.interval:3000}")
    public void pollAndBroadcast() {
        int sessionCount = webSocketHandler.getSessionCount();
        if (sessionCount > 0) {
            log.debug("Polling data, active sessions: {}", sessionCount);
            webSocketHandler.broadcastDashboardData();
        }
    }

    /**
     * 每分钟保存生产数据到数据库
     */
    @Scheduled(fixedRate = 60000)
    public void saveProductionData() {
        try {
            DashboardData data = dashboardService.getDashboardData();
            if (data != null) {
                statisticsService.saveCurrentProductionData(data);

                // 保存设备状态变化
                if (data.getDevices() != null) {
                    for (DeviceInfo device : data.getDevices()) {
                        statisticsService.saveDeviceStatusLog(
                                device.getDeviceId(),
                                device.getDeviceName(),
                                device.getStatus()
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to save production data", e);
        }
    }

    /**
     * 每天凌晨1点生成前一天的统计报告
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Generating daily report for {}", yesterday);

        try {
            DashboardData data = dashboardService.getDashboardData();
            if (data != null) {
                statisticsService.updateDailyStatistics(yesterday, data);
            }
        } catch (Exception e) {
            log.error("Failed to generate daily report", e);
        }
    }

    /**
     * 每月1号凌晨2点生成上月的月度统计
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReport() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        log.info("Generating monthly report for {}-{}", year, month);

        try {
            statisticsService.generateMonthlyStatistics(year, month);
        } catch (Exception e) {
            log.error("Failed to generate monthly report", e);
        }
    }
}
