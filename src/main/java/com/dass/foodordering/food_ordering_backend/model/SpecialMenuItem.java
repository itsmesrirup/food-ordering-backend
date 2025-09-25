package com.dass.foodordering.food_ordering_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SpecialMenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "special_menu_id", nullable = false)
    @JsonIgnore
    private SpecialMenu specialMenu;
    
    private String dayTitle;    // e.g., "Lundi" or "Plat du Jour"
    private String name;        // e.g., "Collet fumé de porc"
    private String description; // e.g., "sauce moutarde à l'ancienne, spaetzles"
}
