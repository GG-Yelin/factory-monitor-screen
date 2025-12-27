package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日统计汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_statistics", indexes = {
    @Index(name = "idx_stats_date", columnList = "statisticsDate", unique = true)
})
public class DailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 统计日期 */
    @Column(nullable = false, unique = true)
    private LocalDate statisticsDate;

    /** 总生产数量 */
    @Column(nullable = false)
    private Integer totalProduction;

    /** 总计划数量 */
    private Integer totalPlan;

    /** 完成率 */
    private Double completionRate;

    /** 良品数量 */
    private Integer qualifiedQuantity;

    /** 良品率 */
    private Double qualityRate;

    /** 设备总数 */
    private Integer totalDevices;

    /** 在线设备数 */
    private Integer onlineDevices;

    /** 报警次数 */
    private Integer alarmCount;

    /** 平均设备效率 OEE */
    private Double avgOee;

    /** 总运行时长(分钟) */
    private Integer totalRunningMinutes;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
