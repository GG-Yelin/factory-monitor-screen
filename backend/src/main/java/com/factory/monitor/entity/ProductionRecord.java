package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产记录实体 - 记录每日生产数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "production_record", indexes = {
    @Index(name = "idx_record_date", columnList = "recordDate"),
    @Index(name = "idx_device_id", columnList = "deviceId")
})
public class ProductionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 记录日期 */
    @Column(nullable = false)
    private LocalDate recordDate;

    /** 设备ID（可选，null表示汇总数据） */
    private String deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 项目ID */
    private String itemId;

    /** 项目名称 */
    private String itemName;

    /** 生产数量 */
    @Column(nullable = false)
    private Integer production;

    /** 计划数量 */
    private Integer planQuantity;

    /** 良品数量 */
    private Integer qualifiedQuantity;

    /** 不良品数量 */
    private Integer defectiveQuantity;

    /** 完成率 */
    private Double completionRate;

    /** 良品率 */
    private Double qualityRate;

    /** 设备运行时长(分钟) */
    private Integer runningMinutes;

    /** 设备停机时长(分钟) */
    private Integer downTimeMinutes;

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
