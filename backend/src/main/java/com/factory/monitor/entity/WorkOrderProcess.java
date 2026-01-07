package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 派工单工序实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_work_order_process", indexes = {
    @Index(name = "idx_wop_work_order_id", columnList = "workOrderId"),
    @Index(name = "idx_wop_operator_id", columnList = "operatorId"),
    @Index(name = "idx_wop_status", columnList = "status")
})
public class WorkOrderProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 派工单ID */
    @Column(nullable = false)
    private Long workOrderId;

    /** 派工单号（冗余） */
    @Column(length = 50)
    private String workOrderNo;

    /** 工序模板步骤ID */
    @Column(nullable = false)
    private Long processStepId;

    /** 工序名称 */
    @Column(nullable = false, length = 200)
    private String processName;

    /** 工序顺序 */
    @Column(nullable = false)
    private Integer sortOrder;

    /** 计划数量 */
    @Column(nullable = false)
    private Integer planQuantity;

    /** 已完成数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer completedQuantity = 0;

    /** 合格数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer qualifiedQuantity = 0;

    /** 报废数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer scrapQuantity = 0;

    /** 计件单价 */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 负责人ID（操作工） */
    private Long operatorId;

    /** 负责人姓名 */
    @Column(length = 50)
    private String operatorName;

    /** 是否需要质检 */
    @Column(nullable = false)
    @Builder.Default
    private Integer needInspection = 0;

    /** 质检员ID */
    private Long inspectorId;

    /** 质检员姓名 */
    @Column(length = 50)
    private String inspectorName;

    /** 质检状态：PENDING-待质检, PASSED-已通过, FAILED-不通过 */
    @Column(length = 20)
    private String inspectionStatus;

    /** 工序二维码内容 */
    @Column(length = 500)
    private String qrCode;

    /** 状态：PENDING-待开工, IN_PROGRESS-进行中, COMPLETED-已完成, PAUSED-暂停 */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** 实际开始时间 */
    private LocalDateTime actualStartTime;

    /** 实际结束时间 */
    private LocalDateTime actualEndTime;

    /** 总工时（分钟） */
    @Builder.Default
    private Integer totalMinutes = 0;

    /** 备注 */
    @Column(length = 1000)
    private String remark;

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
