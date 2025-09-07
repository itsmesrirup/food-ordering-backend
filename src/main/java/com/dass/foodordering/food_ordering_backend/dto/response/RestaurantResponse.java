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

    // --- NEW THEME FIELDS ---
    private String themePrimaryColor;
    private String themeSecondaryColor;
    private String logoUrl;

    // Background and Paper Colors
    private String themeBackgroundColor;    // e.g., "#1a1a1a"
    private String themePaperColor;       // e.g., "#2c2c2c"

    // Text Colors
    private String themeTextColorPrimary;   // e.g., "#ffffff"
    private String themeTextColorSecondary; // e.g., "#bbbbbb"
    
    // Hero/Background Image
    private String themeBackgroundImageUrl; // URL to a large background image

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
        this.themePrimaryColor = restaurant.getThemePrimaryColor();
        this.themeSecondaryColor = restaurant.getThemeSecondaryColor();
        this.logoUrl = restaurant.getLogoUrl();

        this.themeBackgroundColor = restaurant.getThemeBackgroundColor();
        this.themePaperColor = restaurant.getThemePaperColor();
        this.themeTextColorPrimary = restaurant.getThemeTextColorPrimary();
        this.themeTextColorSecondary = restaurant.getThemeTextColorSecondary();
        this.themeBackgroundImageUrl = restaurant.getThemeBackgroundImageUrl();
    }
}