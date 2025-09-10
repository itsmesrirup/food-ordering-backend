package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import lombok.Data;

@Data
public class RestaurantSettingsResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private boolean reservationsEnabled;
    private boolean qrCodeOrderingEnabled;
    private boolean recommendationsEnabled;
    private boolean useDarkTheme;
    private String logoUrl;
    private String heroImageUrl;

    public RestaurantSettingsResponse(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.email = restaurant.getEmail();
        this.reservationsEnabled = restaurant.isReservationsEnabled();
        this.qrCodeOrderingEnabled = restaurant.isQrCodeOrderingEnabled();
        this.recommendationsEnabled = restaurant.isRecommendationsEnabled();
        this.useDarkTheme = restaurant.isUseDarkTheme();
        this.logoUrl = restaurant.getLogoUrl();
        this.heroImageUrl = restaurant.getHeroImageUrl();
    }
}