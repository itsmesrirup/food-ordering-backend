package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrdersByHourResponse {
    private int hour;
    private long orderCount;
}