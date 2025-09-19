package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.CustomerRestaurant;
import com.dass.foodordering.food_ordering_backend.model.CustomerRestaurantId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRestaurantRepository extends JpaRepository<CustomerRestaurant, CustomerRestaurantId> {
    List<CustomerRestaurant> findByCustomerId(Long customerId);
    List<CustomerRestaurant> findByRestaurantId(Long restaurantId);
    Optional<CustomerRestaurant> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);
    boolean existsByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);
}