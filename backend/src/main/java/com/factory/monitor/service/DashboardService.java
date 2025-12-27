package com.factory.monitor.service;

import com.factory.monitor.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DashboardService {

    private final XinjeCloudService xinjeCloudService;

    // 数据缓存
    private DashboardData cachedData;
    private final Map<String, Integer> productionHistory = new ConcurrentHashMap<>();

    public DashboardService(XinjeCloudService xinjeCloudService) {
        this.xinjeCloudService = xinjeCloudService;
    }

    /**
     * 获取大屏数据
     */
    public DashboardData getDashboardData() {
        try {
            // 获取项目列表
            List<ProjectInfo> projects = xinjeCloudService.getProjectList();

            // 使用新接口获取所有设备（包含真实在线状态）
            List<DeviceInfo> allDevices = xinjeCloudService.getAllDevicesWithStatus();
            List<DataPoint> allDataPoints = new ArrayList<>();

            // 按项目分组统计设备
            Map<String, List<DeviceInfo>> devicesByProject = allDevices.stream()
                    .filter(d -> d.getItemId() != null)
                    .collect(java.util.stream.Collectors.groupingBy(DeviceInfo::getItemId));

            for (ProjectInfo project : projects) {
                // 获取实时数据
                List<DataPoint> dataPoints = xinjeCloudService.getItemData(project.getItemId());
                allDataPoints.addAll(dataPoints);

                // 更新项目的设备数量（从设备列表中统计）
                List<DeviceInfo> projectDevices = devicesByProject.getOrDefault(project.getItemId(), new ArrayList<>());
                project.setDeviceCount(projectDevices.size());
                project.setOnlineCount((int) projectDevices.stream().filter(d -> d.getStatus() == 1).count());
            }

            // 统计设备状态
            int totalDevices = allDevices.size();
            int onlineDevices = (int) allDevices.stream().filter(d -> d.getStatus() == 1).count();
            int offlineDevices = (int) allDevices.stream().filter(d -> d.getStatus() == 0).count();
            int alarmDevices = (int) allDevices.stream().filter(d -> d.getStatus() == 2).count();

            // 从数据点中提取生产数据（根据实际数据点名称调整）
            int todayProduction = extractProductionCount(allDataPoints, "production", "count", "产量");
            int planProduction = extractPlanCount(allDataPoints, "plan", "计划");

            // 如果没有从接口获取到数据，使用模拟数据
            if (todayProduction == 0) {
                todayProduction = generateSimulatedProduction();
            }
            if (planProduction == 0) {
                planProduction = 1000; // 默认计划产量
            }

            double productionRate = planProduction > 0 ? (double) todayProduction / planProduction * 100 : 0;

            // 计算效率指标
            double equipmentEfficiency = calculateOEE(allDataPoints, allDevices);
            double qualityRate = extractQualityRate(allDataPoints);
            double runningRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0;

            // 生成生产趋势
            List<ProductionTrend> productionTrend = generateProductionTrend();

            // 生成报警信息
            List<AlarmInfo> alarms = generateAlarms(allDevices);

            cachedData = DashboardData.builder()
                    .totalDevices(totalDevices)
                    .onlineDevices(onlineDevices)
                    .offlineDevices(offlineDevices)
                    .alarmDevices(alarmDevices)
                    .todayProduction(todayProduction)
                    .planProduction(planProduction)
                    .productionRate(Math.round(productionRate * 100.0) / 100.0)
                    .equipmentEfficiency(Math.round(equipmentEfficiency * 100.0) / 100.0)
                    .qualityRate(Math.round(qualityRate * 100.0) / 100.0)
                    .runningRate(Math.round(runningRate * 100.0) / 100.0)
                    .projects(projects)
                    .devices(allDevices)
                    .dataPoints(allDataPoints)
                    .productionTrend(productionTrend)
                    .alarms(alarms)
                    .updateTime(System.currentTimeMillis())
                    .build();

            return cachedData;

        } catch (Exception e) {
            log.error("Get dashboard data error", e);
            return cachedData != null ? cachedData : createEmptyDashboard();
        }
    }

    /**
     * 从数据点提取生产数量
     */
    private int extractProductionCount(List<DataPoint> dataPoints, String... keywords) {
        for (DataPoint dp : dataPoints) {
            String name = dp.getName().toLowerCase();
            for (String keyword : keywords) {
                if (name.contains(keyword.toLowerCase())) {
                    try {
                        return Integer.parseInt(dp.getValue());
                    } catch (NumberFormatException e) {
                        // 忽略非数字值
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 从数据点提取计划数量
     */
    private int extractPlanCount(List<DataPoint> dataPoints, String... keywords) {
        for (DataPoint dp : dataPoints) {
            String name = dp.getName().toLowerCase();
            for (String keyword : keywords) {
                if (name.contains(keyword.toLowerCase())) {
                    try {
                        return Integer.parseInt(dp.getValue());
                    } catch (NumberFormatException e) {
                        // 忽略非数字值
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 计算设备综合效率 OEE
     */
    private double calculateOEE(List<DataPoint> dataPoints, List<DeviceInfo> devices) {
        if (devices.isEmpty()) {
            return 85.0; // 默认值
        }
        // 简化计算：运行率 * 性能率 * 良品率
        double runningRate = devices.stream().filter(d -> d.getStatus() == 1).count() * 1.0 / devices.size();
        double performanceRate = 0.95; // 假设性能率
        double qualityRate = 0.98; // 假设良品率

        return runningRate * performanceRate * qualityRate * 100;
    }

    /**
     * 提取良品率
     */
    private double extractQualityRate(List<DataPoint> dataPoints) {
        for (DataPoint dp : dataPoints) {
            String name = dp.getName().toLowerCase();
            if (name.contains("quality") || name.contains("良品") || name.contains("合格")) {
                try {
                    return Double.parseDouble(dp.getValue());
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }
        return 98.5; // 默认良品率
    }

    /**
     * 生成模拟生产数量
     */
    private int generateSimulatedProduction() {
        // 根据当前时间模拟产量增长
        int hour = LocalDate.now().getDayOfYear() * 24 + java.time.LocalTime.now().getHour();
        Random random = new Random(hour);
        return 500 + random.nextInt(500);
    }

    /**
     * 生成生产趋势数据
     */
    private List<ProductionTrend> generateProductionTrend() {
        List<ProductionTrend> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Random random = new Random();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);

            int plan = 1000;
            int production = 800 + random.nextInt(300);
            double rate = (double) production / plan * 100;

            trends.add(ProductionTrend.builder()
                    .date(dateStr)
                    .production(production)
                    .plan(plan)
                    .rate(Math.round(rate * 100.0) / 100.0)
                    .build());
        }

        return trends;
    }

    /**
     * 生成报警信息
     */
    private List<AlarmInfo> generateAlarms(List<DeviceInfo> devices) {
        List<AlarmInfo> alarms = new ArrayList<>();

        // 检查设备状态生成报警
        for (DeviceInfo device : devices) {
            if (device.getStatus() == 2) {
                alarms.add(AlarmInfo.builder()
                        .id(UUID.randomUUID().toString())
                        .deviceId(device.getDeviceId())
                        .deviceName(device.getDeviceName())
                        .alarmType("设备报警")
                        .alarmContent("设备运行异常，请检查")
                        .alarmTime(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .level(2)
                        .status(0)
                        .build());
            }
        }

        return alarms;
    }

    /**
     * 创建空的大屏数据
     */
    private DashboardData createEmptyDashboard() {
        return DashboardData.builder()
                .totalDevices(0)
                .onlineDevices(0)
                .offlineDevices(0)
                .alarmDevices(0)
                .todayProduction(0)
                .planProduction(0)
                .productionRate(0.0)
                .equipmentEfficiency(0.0)
                .qualityRate(0.0)
                .runningRate(0.0)
                .projects(new ArrayList<>())
                .devices(new ArrayList<>())
                .dataPoints(new ArrayList<>())
                .productionTrend(new ArrayList<>())
                .alarms(new ArrayList<>())
                .updateTime(System.currentTimeMillis())
                .build();
    }
}
