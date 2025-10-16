package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import lombok.Data;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.Set;

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
    private SubscriptionPlan plan;
    private Set<String> availableFeatures;
    private PaymentModel paymentModel;
    private BigDecimal commissionRate;
    
    // --- ADDED: New website fields ---
    private String aboutUsText;
    private String phoneNumber;
    private String openingHours;
    private String googleMapsUrl;
    private String slug;

    private String metaTitle;
    private String metaDescription;

    public RestaurantSettingsResponse(Restaurant restaurant, Set<String> availableFeatures) {
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
        this.plan = restaurant.getPlan();
        this.availableFeatures = availableFeatures;
        this.paymentModel = restaurant.getPaymentModel();
        this.commissionRate = restaurant.getCommissionRate();
        this.aboutUsText = restaurant.getAboutUsText();
        this.phoneNumber = restaurant.getPhoneNumber();
        this.openingHours = restaurant.getOpeningHours();
        this.googleMapsUrl = restaurant.getGoogleMapsUrl();
        this.slug = restaurant.getSlug();
        this.metaTitle = restaurant.getMetaTitle();
        this.metaDescription = restaurant.getMetaDescription();
    }
}