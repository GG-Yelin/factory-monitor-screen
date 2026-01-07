package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料BOM（物料清单）实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_material_bom", indexes = {
    @Index(name = "idx_bom_product_id", columnList = "productId")
})
public class MaterialBom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 产品ID */
    @Column(nullable = false)
    private Long productId;

    /** 物料ID */
    @Column(nullable = false)
    private Long materialId;

    /** 物料名称（冗余） */
    @Column(length = 200)
    private String materialName;

    /** 物料编码（冗余） */
    @Column(length = 50)
    private String materialCode;

    /** 单位用量 */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    /** 单位 */
    @Column(length = 20)
    private String unit;

    /** 备注 */
    @Column(length = 500)
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
