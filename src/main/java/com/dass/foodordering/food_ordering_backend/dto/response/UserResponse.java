package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private Integer id;
    private String email;
    private Long restaurantId;
    private String restaurantName;
    private String role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        if (user.getRestaurant() != null) {
            this.restaurantId = user.getRestaurant().getId();
            this.restaurantName = user.getRestaurant().getName();
        }
        this.role = user.getRole().name();
    }
}