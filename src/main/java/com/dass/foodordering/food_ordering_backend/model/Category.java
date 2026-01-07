package com.dass.foodordering.food_ordering_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore // Prevent infinite loops when serializing
    private Restaurant restaurant;

    // Self-referencing relationship for subcategories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnore
    @ToString.Exclude // Exclude from Lombok's toString to prevent recursion
    @EqualsAndHashCode.Exclude // Exclude from Lombok's equals/hashCode
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MenuItem> menuItems = new ArrayList<>();

    @CreationTimestamp // Automatically sets the time when saved
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // --- ADDED: Soft Delete Flag ---
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;
}