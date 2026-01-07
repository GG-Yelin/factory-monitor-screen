package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报工记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_work_report", indexes = {
    @Index(name = "idx_wr_process_id", columnList = "workOrderProcessId"),
    @Index(name = "idx_wr_operator_id", columnList = "operatorId"),
    @Index(name = "idx_wr_report_time", columnList = "reportTime")
})
public class WorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 派工单工序ID */
    @Column(nullable = false)
    private Long workOrderProcessId;

    /** 派工单ID */
    @Column(nullable = false)
    private Long workOrderId;

    /** 派工单号 */
    @Column(length = 50)
    private String workOrderNo;

    /** 工序名称 */
    @Column(length = 200)
    private String processName;

    /** 操作工ID */
    @Column(nullable = false)
    private Long operatorId;

    /** 操作工姓名 */
    @Column(nullable = false, length = 50)
    private String operatorName;

    /** 完成数量（合格数量） */
    @Column(nullable = false)
    private Integer completedQuantity;

    /** 报废数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer scrapQuantity = 0;

    /** 工时（分钟） */
    @Column(nullable = false)
    @Builder.Default
    private Integer workMinutes = 0;

    /** 计件单价 */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 应得工资 */
    @Column(precision = 10, scale = 2)
    private BigDecimal wage;

    /** 开始时间（计时报工） */
    private LocalDateTime startTime;

    /** 结束时间（计时报工） */
    private LocalDateTime endTime;

    /** 报工时间 */
    @Column(nullable = false)
    private LocalDateTime reportTime;

    /** 自定义字段1值 */
    @Column(length = 200)
    private String customField1Value;

    /** 自定义字段2值 */
    @Column(length = 200)
    private String customField2Value;

    /** 自定义字段3值 */
    @Column(length = 200)
    private String customField3Value;

    /** 备注（可文字或语音转文字） */
    @Column(length = 1000)
    private String remark;

    /** 报工来源：SCAN-扫码, MANUAL-手动 */
    @Column(length = 20)
    @Builder.Default
    private String source = "SCAN";

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reportTime == null) {
            reportTime = LocalDateTime.now();
        }
    }
}
