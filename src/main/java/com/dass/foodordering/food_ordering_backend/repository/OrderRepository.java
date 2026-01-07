package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.SalesByPeriodResponse;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
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
        "COALESCE(AVG(o.totalPrice), 0.0)) " + // AVG returns Double
        "FROM Order o WHERE o.restaurant.id = :restaurantId")
    AnalyticsSummaryResponse getAnalyticsSummary(@Param("restaurantId") Long restaurantId);

    // Query for sales over the last 30 days, grouped by day
    @Query("SELECT new com.dass.foodordering.food_ordering_backend.dto.response.SalesByPeriodResponse(" +
        "CAST(o.orderTime AS LocalDate), " +
        "SUM(o.totalPrice)) " +
        "FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderTime >= :startDate " +
        "GROUP BY CAST(o.orderTime AS LocalDate) " +
        "ORDER BY CAST(o.orderTime AS LocalDate) ASC")
    List<SalesByPeriodResponse> findSalesByDay(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate);

    // Query for order counts, grouped by the hour of the day
    @Query(value = "SELECT " +
                   "EXTRACT(HOUR FROM o.order_time) AS hour, " +
                   "COUNT(*) AS orderCount " +
                   "FROM orders o WHERE o.restaurant_id = :restaurantId " +
                   "GROUP BY EXTRACT(HOUR FROM o.order_time) " +
                   "ORDER BY hour ASC",
           nativeQuery = true)
    List<OrdersByHourResponseProjection> findOrdersByHourNative(@Param("restaurantId") Long restaurantId);

    @EntityGraph(value = "Order.withItemsAndCustomer")
    List<Order> findByRestaurantIdOrderByIdDesc(Long restaurantId);

    List<Order> findByRestaurantIdAndStatusIn(Long restaurantId, List<OrderStatus> statuses);

    // --- ADDED: Get the highest sequence number used so far ---
    // COALESCE handles the case where it's the very first order (returns 0 instead of null)
    @Query("SELECT COALESCE(MAX(o.restaurantOrderSequence), 0) FROM Order o WHERE o.restaurant.id = :restaurantId")
    Long getMaxOrderSequence(@Param("restaurantId") Long restaurantId);
}
