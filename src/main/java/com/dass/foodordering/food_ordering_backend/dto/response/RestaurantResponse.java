package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.math.BigDecimal;
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
    private boolean recommendationsEnabled;

    // --- NEW THEME FIELDS ---
    private boolean useDarkTheme;
    private String logoUrl;
    private String heroImageUrl;

    private boolean active;
    private SubscriptionPlan plan;

    private String aboutUsText;
    private String phoneNumber;
    private String openingHours;
    private String googleMapsUrl;
    private String slug;

    private PaymentModel paymentModel;
    private BigDecimal commissionRate;

    private String metaTitle;
    private String metaDescription;

    
    public RestaurantResponse(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.email = restaurant.getEmail();
        
        // Map feature flags
        this.reservationsEnabled = restaurant.isReservationsEnabled();
        this.qrCodeOrderingEnabled = restaurant.isQrCodeOrderingEnabled();
        this.recommendationsEnabled = restaurant.isRecommendationsEnabled();

        // Convert MenuItem to MenuItemResponse to avoid circular dependencies
        if (restaurant.getMenuItems() != null) {
            this.menuItems = restaurant.getMenuItems().stream()
                    .map(MenuItemResponse::new)
                    .collect(Collectors.toList());
        }
        this.useDarkTheme = restaurant.isUseDarkTheme();
        this.logoUrl = restaurant.getLogoUrl();
        this.heroImageUrl = restaurant.getHeroImageUrl();
        this.active = restaurant.isActive();
        this.plan = restaurant.getPlan();

        this.aboutUsText = restaurant.getAboutUsText();
        this.phoneNumber = restaurant.getPhoneNumber();
        this.openingHours = restaurant.getOpeningHours();
        this.googleMapsUrl = restaurant.getGoogleMapsUrl();
        this.slug = restaurant.getSlug();

        this.paymentModel = restaurant.getPaymentModel();
        this.commissionRate = restaurant.getCommissionRate();
        this.metaTitle = restaurant.getMetaTitle();
        this.metaDescription = restaurant.getMetaDescription();
    }
}