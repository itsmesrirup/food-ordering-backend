package com.dass.foodordering.food_ordering_backend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;

    // Optional link back to the restaurant that offers this item
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // Link to a Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // GETTER/SETTER for category
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    // Defaults to true when a new item is created.
    private boolean available = true;

    // GETTER/SETTER for isAvailable
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Column(nullable = false)
    private boolean isBundle = false; // Flag to identify "Formule" items

    public boolean isBundle() {
        return isBundle;
    }
    public void setBundle(boolean isBundle) {
        this.isBundle = isBundle;
    }
    public List<MenuItemOption> getOptions() {
        return options;
    }
    public void setOptions(List<MenuItemOption> options) {
        this.options = options;
    }

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemOption> options = new ArrayList<>();

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    
}
