package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工资记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_wage_record", indexes = {
    @Index(name = "idx_wage_user_id", columnList = "userId"),
    @Index(name = "idx_wage_date", columnList = "workDate"),
    @Index(name = "idx_wage_user_date", columnList = "userId, workDate")
})
public class WageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 员工ID */
    @Column(nullable = false)
    private Long userId;

    /** 员工姓名 */
    @Column(nullable = false, length = 50)
    private String userName;

    /** 员工工号 */
    @Column(length = 50)
    private String employeeNo;

    /** 工作日期 */
    @Column(nullable = false)
    private LocalDate workDate;

    /** 关联报工记录ID */
    private Long workReportId;

    /** 派工单号 */
    @Column(length = 50)
    private String workOrderNo;

    /** 产品名称 */
    @Column(length = 200)
    private String productName;

    /** 工序名称 */
    @Column(length = 200)
    private String processName;

    /** 完成数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 计件单价 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 应得工资 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal wage;

    /** 工资类型：PIECE-计件, TIME-计时 */
    @Column(length = 20)
    @Builder.Default
    private String wageType = "PIECE";

    /** 状态：PENDING-待结算, SETTLED-已结算 */
    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** 结算时间 */
    private LocalDateTime settledAt;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
