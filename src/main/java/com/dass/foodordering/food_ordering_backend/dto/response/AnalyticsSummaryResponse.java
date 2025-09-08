package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private double totalRevenue;
    private long totalOrders;
    private double averageOrderValue;
}