package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 派工单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_work_order", indexes = {
    @Index(name = "idx_work_order_no", columnList = "workOrderNo"),
    @Index(name = "idx_sales_order_id", columnList = "salesOrderId"),
    @Index(name = "idx_work_order_status", columnList = "status")
})
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 派工单号 */
    @Column(nullable = false, unique = true, length = 50)
    private String workOrderNo;

    /** 关联销售订单ID */
    @Column(nullable = false)
    private Long salesOrderId;

    /** 订单号（冗余字段） */
    @Column(length = 50)
    private String orderNo;

    /** 产品ID */
    @Column(nullable = false)
    private Long productId;

    /** 产品名称 */
    @Column(nullable = false, length = 200)
    private String productName;

    /** 产品编码 */
    @Column(length = 50)
    private String productCode;

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

    /** 计划开始日期 */
    private LocalDate planStartDate;

    /** 计划结束日期 */
    private LocalDate planEndDate;

    /** 实际开始时间 */
    private LocalDateTime actualStartTime;

    /** 实际结束时间 */
    private LocalDateTime actualEndTime;

    /** 分配班组ID */
    private Long teamId;

    /** 班组名称 */
    @Column(length = 100)
    private String teamName;

    /** 分配设备ID */
    @Column(length = 50)
    private String deviceId;

    /** 设备名称 */
    @Column(length = 100)
    private String deviceName;

    /** 派工单总二维码内容 */
    @Column(length = 500)
    private String qrCode;

    /** 状态：PENDING-待开工, IN_PROGRESS-进行中, PAUSED-暂停, COMPLETED-已完成, CANCELLED-已取消 */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** 备注 */
    @Column(length = 1000)
    private String remark;

    /** 创建人ID */
    private Long createdBy;

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
