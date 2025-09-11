package com.dass.foodordering.food_ordering_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class MenuItemOptionChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_item_option_id", nullable = false)
    @JsonIgnore
    private MenuItemOption menuItemOption;

    private String name; // e.g., "SAMOSSA"
    private double priceAdjustment = 0.0; // e.g., +€2.00 for a premium choice
}
