package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_material", indexes = {
    @Index(name = "idx_material_code", columnList = "code")
})
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物料编码 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 物料名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 规格型号 */
    @Column(length = 200)
    private String specification;

    /** 单位 */
    @Column(length = 20)
    private String unit;

    /** 当前库存数量 */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    /** 库存预警阈值 */
    @Column(precision = 12, scale = 2)
    private BigDecimal warningThreshold;

    /** 单价 */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 存放位置 */
    @Column(length = 200)
    private String location;

    /** 描述 */
    @Column(length = 1000)
    private String description;

    /** 状态：0-禁用, 1-启用 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

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
