package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsSummaryResponse {
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
}