package com.dass.foodordering.food_ordering_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Item name cannot be empty")
    private String name;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private double price;
    private Long restaurantId;
    private String description;
    private Long categoryId;
    private boolean bundle;
    private String imageUrl;
}