package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    // 设备统计
    private Integer totalDevices;       // 设备总数
    private Integer onlineDevices;      // 在线设备数
    private Integer offlineDevices;     // 离线设备数
    private Integer alarmDevices;       // 报警设备数

    // 生产统计
    private Integer todayProduction;    // 今日生产数量
    private Integer planProduction;     // 计划生产数量
    private Double productionRate;      // 完成率

    // 效率统计
    private Double equipmentEfficiency; // 设备综合效率 OEE
    private Double qualityRate;         // 良品率
    private Double runningRate;         // 运行率

    // 项目列表
    private List<ProjectInfo> projects;

    // 设备列表
    private List<DeviceInfo> devices;

    // 实时数据点
    private List<DataPoint> dataPoints;

    // 生产趋势（最近7天）
    private List<ProductionTrend> productionTrend;

    // 报警信息
    private List<AlarmInfo> alarms;

    // 更新时间
    private Long updateTime;
}
