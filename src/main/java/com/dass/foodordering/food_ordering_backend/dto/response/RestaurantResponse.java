package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private List<MenuItemResponse> menuItems;

    // --- New Feature Flags ---
    private boolean reservationsEnabled;
    private boolean qrCodeOrderingEnabled;

    public RestaurantResponse(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.email = restaurant.getEmail();
        
        // Map feature flags
        this.reservationsEnabled = restaurant.isReservationsEnabled();
        this.qrCodeOrderingEnabled = restaurant.isQrCodeOrderingEnabled();

        // Convert MenuItem to MenuItemResponse to avoid circular dependencies
        if (restaurant.getMenuItems() != null) {
            this.menuItems = restaurant.getMenuItems().stream()
                    .map(MenuItemResponse::new)
                    .collect(Collectors.toList());
        }
    }
}