package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Good practice to add a no-arg constructor as well

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersByHourResponse {
    // ✅ Change from primitive 'int' to wrapper 'Integer'
    private Integer hour;

    // ✅ Change from primitive 'long' to wrapper 'Long'
    private Long orderCount;
}