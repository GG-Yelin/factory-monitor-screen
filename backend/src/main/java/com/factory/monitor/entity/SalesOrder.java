package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_sales_order", indexes = {
    @Index(name = "idx_order_no", columnList = "orderNo"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_delivery_date", columnList = "deliveryDate")
})
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号（自动生成） */
    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    /** 产品ID */
    @Column(nullable = false)
    private Long productId;

    /** 产品名称（冗余字段） */
    @Column(nullable = false, length = 200)
    private String productName;

    /** 产品编码（冗余字段） */
    @Column(length = 50)
    private String productCode;

    /** 订单数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 已完成数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer completedQuantity = 0;

    /** 合格数量 */
    @Column(nullable = false)
    @Builder.Default
    private Integer qualifiedQuantity = 0;

    /** 交货日期 */
    @Column(nullable = false)
    private LocalDate deliveryDate;

    /** 客户名称 */
    @Column(length = 200)
    private String customerName;

    /** 订单状态：DRAFT-草稿, PENDING-待排产, SCHEDULED-已排产, IN_PROGRESS-生产中, COMPLETED-已完成, CANCELLED-已取消 */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    /** 优先级：1-低, 2-中, 3-高, 4-紧急 */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 2;

    /** 备注 */
    @Column(length = 1000)
    private String remark;

    /** 创建人ID */
    private Long createdBy;

    /** 创建人姓名 */
    @Column(length = 50)
    private String createdByName;

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
