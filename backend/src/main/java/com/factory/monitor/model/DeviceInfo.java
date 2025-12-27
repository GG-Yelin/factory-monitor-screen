package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String itemId;
    private String itemName;
    private Integer status;  // 0: 离线, 1: 在线, 2: 报警
}
