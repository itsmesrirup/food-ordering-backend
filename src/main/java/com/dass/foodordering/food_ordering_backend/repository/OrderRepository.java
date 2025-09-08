package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse;
import com.dass.foodordering.food_ordering_backend.model.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Spring Data JPA will automatically create the query for you based on the method name!
    List<Order> findByRestaurantId(Long restaurantId);

    // Custom query for sales summary
    @Query("SELECT new com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse(" +
           "COALESCE(SUM(o.totalPrice), 0.0), " +
           "COALESCE(COUNT(o), 0L), " +
           "COALESCE(AVG(o.totalPrice), 0.0)) " +
           "FROM Order o WHERE o.restaurant.id = :restaurantId")
    AnalyticsSummaryResponse getAnalyticsSummary(@Param("restaurantId") Long restaurantId);
}
