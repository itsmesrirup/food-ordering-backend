package com.dass.foodordering.food_ordering_backend.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SpecialMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    private String title;       // e.g., "Nos Plats du Jour"
    private String subtitle;    // e.g., "Découvrez nos spécialités de la semaine"
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive = true;

    // This annotation forces the JSON output to be "isActive"
    @JsonProperty("isActive")
    public boolean isActive() {
        return this.isActive;
    }

    @OneToMany(mappedBy = "specialMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialMenuItem> items = new ArrayList<>();
}