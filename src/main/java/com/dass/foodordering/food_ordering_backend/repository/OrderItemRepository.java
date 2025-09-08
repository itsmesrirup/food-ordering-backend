package com.dass.foodordering.food_ordering_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse;
import com.dass.foodordering.food_ordering_backend.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Custom query for top selling items
    @Query("SELECT new com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse(" +
           "oi.menuItem.name, " +
           "SUM(oi.quantity)) " +
           "FROM OrderItem oi WHERE oi.order.restaurant.id = :restaurantId " +
           "GROUP BY oi.menuItem.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingItemResponse> findTopSellingItems(@Param("restaurantId") Long restaurantId);
}
