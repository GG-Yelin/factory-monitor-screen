package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班组实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 班组名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 班组编码 */
    @Column(unique = true, length = 50)
    private String code;

    /** 班组长ID */
    private Long leaderId;

    /** 班组长姓名（冗余字段，方便查询） */
    @Column(length = 50)
    private String leaderName;

    /** 描述 */
    @Column(length = 500)
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
