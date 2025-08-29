package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class MenuItemRequest {
    private String name;
    private double price;
    private Long restaurantId;
    private String description;
}