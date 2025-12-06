package com.dass.foodordering.food_ordering_backend.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class UpdateRestaurantRequest {
    private String name;
    private String address;
    private String email;

    // --- New Feature Flags ---
    private boolean reservationsEnabled;
    private boolean qrCodeOrderingEnabled;
    private boolean recommendationsEnabled;
    private boolean websiteBuilderEnabled;

    // --- NEW THEME FIELDS ---
    // We can store simple color hex codes as Strings.
    private boolean useDarkTheme;
    private String logoUrl;   
    private String heroImageUrl; // URL to a large background image

    private String aboutUsText;
    private String phoneNumber;
    private String openingHours;
    private String googleMapsUrl;
    private String slug;

    private String metaTitle;
    private String metaDescription;

    private String instagramUrl;
    private String facebookUrl;
    private String twitterUrl;

    private List<String> galleryImageUrls;
}