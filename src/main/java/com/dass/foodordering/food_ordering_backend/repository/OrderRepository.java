package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Spring Data JPA will automatically create the query for you based on the method name!
    List<Order> findByRestaurantId(Long restaurantId);
}
