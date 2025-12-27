package com.factory.monitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "xinje.cloud")
public class XinjeConfig {
    private String baseUrl;
    private String username;
    private String password;

    // 数据点配置 - 用于指定产量和计划数的数据点名称（支持多个，用逗号分隔）
    // 对应 yml 中的 production-data-point-names 和 plan-data-point-names
    private String productionDataPointNames;
    private String planDataPointNames;

    public String getProductionDataPointNames() {
        if (productionDataPointNames == null || productionDataPointNames.isEmpty()) {
            return "HD200,production,count,产量,今日产量";
        }
        return productionDataPointNames;
    }

    public String getPlanDataPointNames() {
        if (planDataPointNames == null || planDataPointNames.isEmpty()) {
            return "信捷 XD/XL/XG系列（Modbus RTU）-生产计划数";
        }
        return planDataPointNames;
    }
}
