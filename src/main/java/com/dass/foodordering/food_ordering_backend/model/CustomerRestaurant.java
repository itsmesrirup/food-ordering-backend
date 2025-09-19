package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_restaurant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRestaurant {

    @EmbeddedId
    private CustomerRestaurantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restaurantId")
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private LocalDateTime registeredAt = LocalDateTime.now();

    private Integer loyaltyPoints = 0;

    // Add other restaurant-specific fields as needed (e.g., preferences, VIP status)
}