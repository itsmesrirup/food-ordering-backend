package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private boolean hasOwnUi = false; // Default to false
    private String email; // The contact email for the restaurant

    // --- FEATURE FLAGS ---
    
    @Column(nullable = false)
    private boolean reservationsEnabled = false; // Default to OFF

    @Column(nullable = false)
    private boolean qrCodeOrderingEnabled = false; // Default to OFF

    @Column(nullable = false)
    private boolean recommendationsEnabled = false; // Default to OFF
    
    // Example for a future feature
    // @Column(nullable = false)
    // private boolean loyaltyEnabled = false; 

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Important to prevent infinite loops in JSON serialization
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();

    // Getters and setters
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }
    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    // --- NEW THEME FIELDS ---
    @Column(nullable = false)
    private boolean useDarkTheme = false;
    private String logoUrl; // A URL to the restaurant's logo image
    private String heroImageUrl;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialMenu> specialMenus = new ArrayList<>();

}
