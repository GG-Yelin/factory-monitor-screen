package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 月度统计汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monthly_statistics", indexes = {
    @Index(name = "idx_monthly_stats", columnList = "year,month", unique = true)
})
public class MonthlyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 年份 */
    @Column(nullable = false)
    private Integer year;

    /** 月份 */
    @Column(nullable = false)
    private Integer month;

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

    /** 平均日产量 */
    private Double avgDailyProduction;

    /** 最高日产量 */
    private Integer maxDailyProduction;

    /** 最低日产量 */
    private Integer minDailyProduction;

    /** 报警总次数 */
    private Integer totalAlarmCount;

    /** 平均设备效率 OEE */
    private Double avgOee;

    /** 工作天数 */
    private Integer workDays;

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
