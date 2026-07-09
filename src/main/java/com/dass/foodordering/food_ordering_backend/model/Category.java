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
    @OrderBy("sortOrder ASC") // ✅ ADDED: Tells Hibernate to automatically sort subcategories!
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

    // ✅ ADDED: Remembers the category's position
    @Column(name = "sort_order", columnDefinition = "integer default 0")
    private Integer sortOrder = 0;
}