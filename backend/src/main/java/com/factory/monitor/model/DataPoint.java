package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPoint {
    private String id;
    private String name;
    private Integer dataType;
    private Boolean set;
    private Boolean isCoil;
    private String unit;
    private String value;
    private String valueString;
    private String deviceId;
    private String deviceName;
}
