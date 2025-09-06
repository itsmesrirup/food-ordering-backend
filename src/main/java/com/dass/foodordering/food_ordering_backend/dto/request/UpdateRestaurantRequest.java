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

    // --- NEW THEME FIELDS ---
    // We can store simple color hex codes as Strings.
    private String themePrimaryColor;   // e.g., "#222222"
    private String themeSecondaryColor; // e.g., "#D4AF37"
    private String logoUrl;   
}