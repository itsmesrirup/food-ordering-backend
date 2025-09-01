package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class UpdateRestaurantRequest {
    private String name;
    private String address;
    private String email;

    // --- New Feature Flags ---
    private boolean reservationsEnabled;
    private boolean qrCodeOrderingEnabled;
}