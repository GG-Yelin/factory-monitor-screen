package com.factory.monitor.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionTrend {
    private String date;
    private Integer production;
    private Integer plan;
    private Double rate;
}
