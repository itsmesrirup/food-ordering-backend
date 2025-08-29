package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long menuItemId;
    private int quantity;
}
