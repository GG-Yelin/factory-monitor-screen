package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmInfo {
    private String id;
    private String deviceId;
    private String deviceName;
    private String alarmType;
    private String alarmContent;
    private String alarmTime;
    private Integer level;  // 1: 一般, 2: 重要, 3: 紧急
    private Integer status; // 0: 未处理, 1: 已处理
}
