package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序步骤实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mes_process_step", indexes = {
    @Index(name = "idx_template_id", columnList = "templateId")
})
public class ProcessStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属模板ID */
    @Column(nullable = false)
    private Long templateId;

    /** 工序名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 工序编码 */
    @Column(length = 50)
    private String code;

    /** 工序顺序 */
    @Column(nullable = false)
    private Integer sortOrder;

    /** 计件单价 */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 标准工时（分钟） */
    private Integer standardTime;

    /** 是否需要质检：0-否, 1-是 */
    @Column(nullable = false)
    @Builder.Default
    private Integer needInspection = 0;

    /** 质检标准/要求 */
    @Column(length = 2000)
    private String inspectionStandard;

    /** 工艺要求 */
    @Column(length = 2000)
    private String requirement;

    /** 图纸链接 */
    @Column(length = 500)
    private String drawingUrl;

    /** 自定义字段1名称 */
    @Column(length = 50)
    private String customField1Name;

    /** 自定义字段1类型：text, number, select */
    @Column(length = 20)
    private String customField1Type;

    /** 自定义字段1选项（JSON数组，用于select类型） */
    @Column(length = 500)
    private String customField1Options;

    /** 自定义字段2名称 */
    @Column(length = 50)
    private String customField2Name;

    /** 自定义字段2类型 */
    @Column(length = 20)
    private String customField2Type;

    /** 自定义字段2选项 */
    @Column(length = 500)
    private String customField2Options;

    /** 自定义字段3名称 */
    @Column(length = 50)
    private String customField3Name;

    /** 自定义字段3类型 */
    @Column(length = 20)
    private String customField3Type;

    /** 自定义字段3选项 */
    @Column(length = 500)
    private String customField3Options;

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
