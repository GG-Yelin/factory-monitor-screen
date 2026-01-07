package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user", indexes = {
    @Index(name = "idx_employee_no", columnList = "employeeNo"),
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_username", columnList = "username")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 工号 */
    @Column(unique = true, length = 50)
    private String employeeNo;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String realName;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 密码（加密存储） */
    @Column(nullable = false, length = 100)
    private String password;

    /** 角色：ADMIN-管理员, MANAGER-厂长/主管, LEADER-班组长, OPERATOR-操作工, INSPECTOR-质检员 */
    @Column(nullable = false, length = 20)
    private String role;

    /** 班组ID */
    private Long teamId;

    /** 头像URL */
    @Column(length = 255)
    private String avatar;

    /** 状态：0-禁用, 1-启用 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

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
