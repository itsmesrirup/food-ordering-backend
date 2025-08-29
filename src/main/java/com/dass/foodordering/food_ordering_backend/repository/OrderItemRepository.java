package com.dass.foodordering.food_ordering_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dass.foodordering.food_ordering_backend.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
