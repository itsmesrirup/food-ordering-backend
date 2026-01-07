package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

@Entity
@Table(name = "restaurant")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Restaurant {
    @Id
    // If you implemented the large ID sequence, keep those annotations.
    // If not, IDENTITY is fine for the internal ID.
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private String name;
    private String address;
    private String email;

    // --- FEATURE FLAGS ---
    @Column(name = "website_builder_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean websiteBuilderEnabled = false;
    @Column(nullable = false)
    private boolean reservationsEnabled = false;
    @Column(nullable = false)
    private boolean qrCodeOrderingEnabled = false;
    @Column(nullable = false)
    private boolean recommendationsEnabled = false;
    
    // --- THEME FIELDS ---
    @Column(nullable = false)
    private boolean useDarkTheme = false;
    private String logoUrl;
    private String heroImageUrl;

    // ✅ NEW FIELD for Soft Deletion. Named 'active' to avoid issues.
    // The database column will be named 'is_active' for clarity.
   @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    // --- RELATIONSHIPS ---
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialMenu> specialMenus = new ArrayList<>();

    // --- Field to store the restaurant's subscription plan ---
    // Defaults all new restaurants to the BASIC plan.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'BASIC'")
    private SubscriptionPlan plan = SubscriptionPlan.BASIC;

    // --- ADDED: Fields for the website builder content ---
    @Column(length = 2000) // Use length for long text fields
    private String aboutUsText;
    
    private String phoneNumber;
    
    @Column(length = 500)
    private String openingHours; // Storing as simple text is fine for V1

    @Column(columnDefinition = "TEXT")
    private String googleMapsUrl;

    // A unique, URL-friendly identifier for the restaurant's public page
    @Column(unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'SUBSCRIPTION'")
    private PaymentModel paymentModel = PaymentModel.SUBSCRIPTION;

    // --- ADDED: Field to store the commission rate for this specific restaurant ---
    @Column(precision = 5, scale = 4) // e.g., allows for 0.0500 (5%)
    private BigDecimal commissionRate;

    private String metaTitle;       // e.g., "Au Punjab | Authentic Indian Cuisine in Strasbourg"
    private String metaDescription; // e.g., "Experience the rich flavors of Northern India at Au Punjab..."

    private String instagramUrl;
    private String facebookUrl;
    private String twitterUrl;

    // --- ADDED: A list to store gallery image URLs ---
    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch is fine for a small list (e.g. < 10 images)
    @CollectionTable(name = "restaurant_gallery_images", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> galleryImageUrls = new ArrayList<>();

    // We store it as a raw JSON string in the DB, 
    // but we can add a helper method to parse it if needed, 
    // or just send the string to the frontend to let JS handle it.
    @Column(columnDefinition = "TEXT")
    private String openingHoursJson;

    // The ID provided by Stripe (e.g., "acct_123456789")
    private String stripeAccountId; 
    
    // To quickly check if they are ready to accept payments
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean stripeDetailsSubmitted = false;

    // Master switch controlled by Super Admin
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean paymentsEnabled = false; 

    // The manual getters/setters for menuItems are no longer needed
    // because the @Data annotation from Lombok generates them for you.
}