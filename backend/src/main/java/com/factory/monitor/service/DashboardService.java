package com.factory.monitor.service;

import com.factory.monitor.config.XinjeConfig;
import com.factory.monitor.entity.AlarmRecord;
import com.factory.monitor.model.*;
import com.factory.monitor.repository.AlarmRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DashboardService {

    private final XinjeCloudService xinjeCloudService;
    private final XinjeConfig xinjeConfig;
    private final AlarmRecordRepository alarmRecordRepository;

    // 数据缓存
    private DashboardData cachedData;
    private final Map<String, Integer> productionHistory = new ConcurrentHashMap<>();
    // 记录已报警的设备，避免重复报警（key: deviceId, value: 最近报警时间）
    private final Map<String, LocalDateTime> alarmCache = new ConcurrentHashMap<>();

    public DashboardService(XinjeCloudService xinjeCloudService, XinjeConfig xinjeConfig,
                           AlarmRecordRepository alarmRecordRepository) {
        this.xinjeCloudService = xinjeCloudService;
        this.xinjeConfig = xinjeConfig;
        this.alarmRecordRepository = alarmRecordRepository;
    }

    /**
     * 获取大屏数据
     */
    public DashboardData getDashboardData() {
        try {
            // 获取项目列表
            List<ProjectInfo> projects = xinjeCloudService.getProjectList();
            log.info("Found {} projects: {}", projects.size(),
                    projects.stream().map(p -> p.getItemId() + "(" + p.getItemName() + ")").collect(java.util.stream.Collectors.joining(", ")));

            // 使用新接口获取所有设备（包含真实在线状态）
            List<DeviceInfo> allDevices = xinjeCloudService.getAllDevicesWithStatus();
            List<DataPoint> allDataPoints = new ArrayList<>();

            // 按项目分组统计设备
            Map<String, List<DeviceInfo>> devicesByProject = allDevices.stream()
                    .filter(d -> d.getItemId() != null)
                    .collect(java.util.stream.Collectors.groupingBy(DeviceInfo::getItemId));

            for (ProjectInfo project : projects) {
                // 获取加工数据
                List<DataPoint> itemData = xinjeCloudService.getItemData(project.getItemId());
                allDataPoints.addAll(itemData);

                // 获取基础数据（生产计划数可能在这里）
                List<DataPoint> baseData = xinjeCloudService.getBaseData(project.getItemId());
                allDataPoints.addAll(baseData);

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

            // 从数据点中提取生产数据（使用配置的数据点名称）
            String productionConfig = xinjeConfig.getProductionDataPointNames();
            String planConfig = xinjeConfig.getPlanDataPointNames();

            log.info("Config - productionDataPointNames: '{}', planDataPointNames: '{}'", productionConfig, planConfig);

            String[] productionKeywords = productionConfig.split(",");
            String[] planKeywords = planConfig.split(",");

            log.info("Looking for production data points: {}", Arrays.toString(productionKeywords));
            log.info("Looking for plan data points: {}", Arrays.toString(planKeywords));
            log.info("Total data points count: {}", allDataPoints.size());

            // 打印所有数据点名称，方便排查
            for (DataPoint dp : allDataPoints) {
                log.debug("DataPoint: name='{}', value='{}'", dp.getName(), dp.getValue());
            }

            int todayProduction = extractDataPointValue(allDataPoints, productionKeywords);
            int planProduction = extractDataPointValue(allDataPoints, planKeywords);

            log.info("Extracted - Today production: {}, Plan production: {}", todayProduction, planProduction);

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
     * 从数据点提取数值（支持精确匹配和包含匹配，对所有匹配的数据点求和）
     */
    private int extractDataPointValue(List<DataPoint> dataPoints, String... keywords) {
        int total = 0;
        Set<String> matchedIds = new HashSet<>(); // 避免重复计算同一个数据点

        // 第一轮：精确匹配
        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim();
            for (DataPoint dp : dataPoints) {
                if (dp.getName().equals(trimmedKeyword) && !matchedIds.contains(dp.getId())) {
                    try {
                        String value = dp.getValue();
                        if (value != null && !value.isEmpty()) {
                            int val = (int) Double.parseDouble(value);
                            total += val;
                            matchedIds.add(dp.getId());
                            log.debug("Exact match: '{}' = {} (total: {})", dp.getName(), val, total);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse value '{}' for data point '{}'", dp.getValue(), dp.getName());
                    }
                }
            }
        }

        // 第二轮：包含匹配（排除已匹配的）
        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            for (DataPoint dp : dataPoints) {
                if (dp.getName().toLowerCase().contains(trimmedKeyword) && !matchedIds.contains(dp.getId())) {
                    try {
                        String value = dp.getValue();
                        if (value != null && !value.isEmpty()) {
                            int val = (int) Double.parseDouble(value);
                            total += val;
                            matchedIds.add(dp.getId());
                            log.debug("Contains match: '{}' = {} (total: {})", dp.getName(), val, total);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse value '{}' for data point '{}'", dp.getValue(), dp.getName());
                    }
                }
            }
        }

        log.info("Total matched {} data points, sum = {}", matchedIds.size(), total);
        return total;
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
     * 生成报警信息并保存到数据库
     */
    private List<AlarmInfo> generateAlarms(List<DeviceInfo> devices) {
        List<AlarmInfo> alarms = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // 报警间隔时间（分钟），同一设备在此时间内不重复报警
        long alarmIntervalMinutes = 30;

        // 检查设备状态生成报警
        for (DeviceInfo device : devices) {
            if (device.getStatus() == 2) {
                String deviceId = device.getDeviceId();

                // 检查是否需要生成新报警（避免重复）
                boolean shouldCreateAlarm = true;
                LocalDateTime lastAlarmTime = alarmCache.get(deviceId);
                if (lastAlarmTime != null) {
                    // 如果最近已经报过警，检查时间间隔
                    if (lastAlarmTime.plusMinutes(alarmIntervalMinutes).isAfter(now)) {
                        shouldCreateAlarm = false;
                    }
                }

                if (shouldCreateAlarm) {
                    // 保存报警记录到数据库
                    AlarmRecord alarmRecord = AlarmRecord.builder()
                            .deviceId(deviceId)
                            .deviceName(device.getDeviceName())
                            .alarmType("设备报警")
                            .alarmContent("设备运行异常，请检查")
                            .level(2)
                            .status(0)
                            .alarmTime(now)
                            .build();

                    try {
                        alarmRecordRepository.save(alarmRecord);
                        alarmCache.put(deviceId, now);
                        log.info("保存报警记录: 设备={}, 报警类型={}", deviceId, "设备报警");
                    } catch (Exception e) {
                        log.error("保存报警记录失败: 设备={}", deviceId, e);
                    }
                }

                // 返回给前端显示
                alarms.add(AlarmInfo.builder()
                        .id(UUID.randomUUID().toString())
                        .deviceId(deviceId)
                        .deviceName(device.getDeviceName())
                        .alarmType("设备报警")
                        .alarmContent("设备运行异常，请检查")
                        .alarmTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .level(2)
                        .status(0)
                        .build());
            }
        }

        // 清理过期的报警缓存（超过1小时的）
        alarmCache.entrySet().removeIf(entry ->
                entry.getValue().plusHours(1).isBefore(now));

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
