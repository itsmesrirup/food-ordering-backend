package com.dass.foodordering.food_ordering_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse;
import com.dass.foodordering.food_ordering_backend.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    //  Custom query for top selling items
    @Query("SELECT new com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse(" +
           "oi.menuItem.name, " +
           "SUM(oi.quantity)) " +
           "FROM OrderItem oi WHERE oi.order.restaurant.id = :restaurantId " +
           "GROUP BY oi.menuItem.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingItemResponse> findTopSellingItems(@Param("restaurantId") Long restaurantId);

    @Query("SELECT oi2.menuItem.id " +
        "FROM OrderItem oi1, OrderItem oi2 " +
        "WHERE oi1.order.id = oi2.order.id " +
        "AND oi1.menuItem.id = :menuItemId " +
        "AND oi1.menuItem.id <> oi2.menuItem.id " +
        "GROUP BY oi2.menuItem.id " +
        "ORDER BY COUNT(oi2.menuItem.id) DESC")
    List<Long> findFrequentlyBoughtWith(@Param("menuItemId") Long menuItemId);
}
