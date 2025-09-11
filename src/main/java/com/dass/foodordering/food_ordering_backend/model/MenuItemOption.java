package com.dass.foodordering.food_ordering_backend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
public class MenuItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    @JsonIgnore
    private MenuItem menuItem; // The bundle item this option belongs to (e.g., "Menu Formule")

    private String name; // e.g., "Entrée au choix"
    private int minChoices = 1; // e.g., Must choose at least 1
    private int maxChoices = 1; // e.g., Can choose at most 1

    @OneToMany(mappedBy = "menuItemOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemOptionChoice> choices = new ArrayList<>();
}
