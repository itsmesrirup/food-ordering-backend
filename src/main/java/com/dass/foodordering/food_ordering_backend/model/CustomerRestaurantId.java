package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class CustomerRestaurantId implements Serializable {
    private Long customerId;
    private Long restaurantId;

    public CustomerRestaurantId(Long customerId, Long restaurantId) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
    }
}