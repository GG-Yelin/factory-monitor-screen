package com.factory.monitor.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备状态日志 - 记录设备状态变化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "device_status_log", indexes = {
    @Index(name = "idx_status_device", columnList = "deviceId"),
    @Index(name = "idx_status_time", columnList = "logTime")
})
public class DeviceStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 设备ID */
    @Column(nullable = false)
    private String deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 状态 (0-离线, 1-在线, 2-报警) */
    @Column(nullable = false)
    private Integer status;

    /** 记录时间 */
    @Column(nullable = false)
    private LocalDateTime logTime;

    @PrePersist
    protected void onCreate() {
        if (logTime == null) {
            logTime = LocalDateTime.now();
        }
    }
}
