package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SalesByPeriodResponse {
    private LocalDate period; // Represents the day, week, or month
    private double totalSales;
}