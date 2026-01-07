package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 质检记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_quality_inspection", indexes = {
    @Index(name = "idx_qi_work_order_id", columnList = "workOrderId"),
    @Index(name = "idx_qi_inspector_id", columnList = "inspectorId"),
    @Index(name = "idx_qi_inspection_time", columnList = "inspectionTime")
})
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 派工单ID */
    @Column(nullable = false)
    private Long workOrderId;

    /** 派工单号 */
    @Column(length = 50)
    private String workOrderNo;

    /** 派工单工序ID（工序质检时使用） */
    private Long workOrderProcessId;

    /** 工序名称 */
    @Column(length = 200)
    private String processName;

    /** 质检类型：PROCESS-工序质检, FINAL-最终质检 */
    @Column(nullable = false, length = 20)
    private String inspectionType;

    /** 质检员ID */
    @Column(nullable = false)
    private Long inspectorId;

    /** 质检员姓名 */
    @Column(nullable = false, length = 50)
    private String inspectorName;

    /** 送检数量 */
    @Column(nullable = false)
    private Integer inspectedQuantity;

    /** 合格数量 */
    @Column(nullable = false)
    private Integer qualifiedQuantity;

    /** 不合格数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer unqualifiedQuantity = 0;

    /** 不合格原因 */
    @Column(length = 1000)
    private String unqualifiedReason;

    /** 不合格品图片URL（JSON数组） */
    @Column(length = 2000)
    private String imageUrls;

    /** 质检结果：PASSED-通过, FAILED-不通过 */
    @Column(nullable = false, length = 20)
    private String result;

    /** 质检时间 */
    @Column(nullable = false)
    private LocalDateTime inspectionTime;

    /** 备注 */
    @Column(length = 1000)
    private String remark;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (inspectionTime == null) {
            inspectionTime = LocalDateTime.now();
        }
    }
}
