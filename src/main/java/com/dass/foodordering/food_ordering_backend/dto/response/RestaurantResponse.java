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
    private List<MenuItemResponse> menuItems;

    public RestaurantResponse(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();

        // Convert MenuItem to MenuItemResponse
        if (restaurant.getMenuItems() != null) {
            this.menuItems = restaurant.getMenuItems().stream()
                    .map(MenuItemResponse::new)
                    .collect(Collectors.toList());
        }
    }
}
