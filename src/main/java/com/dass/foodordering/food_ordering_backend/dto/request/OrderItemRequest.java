package com.dass.foodordering.food_ordering_backend.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long menuItemId;
    private int quantity;
    private List<String> selectedOptions;
}
