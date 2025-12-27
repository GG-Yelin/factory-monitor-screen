package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报警记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alarm_record", indexes = {
    @Index(name = "idx_alarm_time", columnList = "alarmTime"),
    @Index(name = "idx_alarm_device", columnList = "deviceId"),
    @Index(name = "idx_alarm_status", columnList = "status")
})
public class AlarmRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 设备ID */
    @Column(nullable = false)
    private String deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 报警类型 */
    private String alarmType;

    /** 报警内容 */
    @Column(length = 500)
    private String alarmContent;

    /** 报警等级 (1-低, 2-中, 3-高) */
    private Integer level;

    /** 状态 (0-未处理, 1-已处理, 2-已忽略) */
    @Column(nullable = false)
    private Integer status;

    /** 报警时间 */
    @Column(nullable = false)
    private LocalDateTime alarmTime;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理人 */
    private String handler;

    /** 处理备注 */
    @Column(length = 500)
    private String handleRemark;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = 0;
        }
    }
}
