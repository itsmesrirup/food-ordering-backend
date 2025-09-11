package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Store the selected choices as a simple JSON string
    // e.g., "[{\"option\":\"Entrée au choix\",\"choice\":\"SAMOSSA\"}, ...]"
    @Column(columnDefinition = "TEXT")
    private String selectedOptions;
}
