package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MenuItemResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private Long restaurantId;

    @JsonProperty("isAvailable")
    private boolean available;

    private Long categoryId;
    private String categoryName;
    private boolean bundle;

    public MenuItemResponse(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.price = menuItem.getPrice();
        this.description = menuItem.getDescription();
        this.available = menuItem.isAvailable();
        this.bundle = menuItem.isBundle();
        if (menuItem.getRestaurant() != null) {
            this.restaurantId = menuItem.getRestaurant().getId();
        } else {
            this.restaurantId = null; // or -1 if you prefer
        }
        if (menuItem.getCategory() != null) {
            this.categoryId = menuItem.getCategory().getId();
            this.categoryName = menuItem.getCategory().getName();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }
    public Long getRestaurantId() { return restaurantId; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    //  getter for the DTO field
    public boolean isAvailable() {
        return available;
    }
    public boolean isBundle() {
        return bundle;
    }
}
