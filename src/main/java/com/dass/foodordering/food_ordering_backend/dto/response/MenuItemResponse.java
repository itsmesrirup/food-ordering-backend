package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.MenuItem;

public class MenuItemResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private Long restaurantId;
    private boolean isAvailable;
    private Long categoryId;
    private String categoryName;

    public MenuItemResponse(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.price = menuItem.getPrice();
        this.description = menuItem.getDescription();
        this.isAvailable = menuItem.isAvailable();
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
    public boolean isAvailable() { return isAvailable; }
}
