package com.dass.foodordering.food_ordering_backend.model;

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

    // <-- This is required so OrderController can call setOrder(...)
    //@ManyToOne
    //@JoinColumn(name = "order_id")
    //private Order order;

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

    //public Order getOrder() { return order; }
    //public void setOrder(Order order) { this.order = order; }
}
