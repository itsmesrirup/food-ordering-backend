package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesByPeriodResponse {
    private LocalDate period;
    
    // ✅ Change from primitive 'double' to wrapper 'Double'
    private Double totalSales;
}