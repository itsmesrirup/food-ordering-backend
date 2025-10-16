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
    @Column(nullable = false)
    private boolean hasOwnUi = false;
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

    // The manual getters/setters for menuItems are no longer needed
    // because the @Data annotation from Lombok generates them for you.
}