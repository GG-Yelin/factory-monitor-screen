package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 产品实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_product", indexes = {
    @Index(name = "idx_product_code", columnList = "code")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 产品编码 */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** 产品名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 规格型号 */
    @Column(length = 200)
    private String specification;

    /** 单位 */
    @Column(length = 20)
    private String unit;

    /** 绑定的工序模板ID */
    private Long templateId;

    /** 产品图片URL */
    @Column(length = 500)
    private String imageUrl;

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
