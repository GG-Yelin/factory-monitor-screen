package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfo {
    private String itemId;
    private String itemName;
    private String lnglat;
    private String parentGroupId;
    private Integer deviceCount;
    private Integer onlineCount;
}
