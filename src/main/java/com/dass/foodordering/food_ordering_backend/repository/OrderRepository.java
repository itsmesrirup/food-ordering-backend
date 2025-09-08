package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrdersByHourResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.SalesByPeriodResponse;
import com.dass.foodordering.food_ordering_backend.model.Order;

import java.time.LocalDateTime;
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
    @Query("SELECT new com.dass.foodordering.food_ordering_backend.dto.response.OrdersByHourResponse(" +
        "FUNCTION('HOUR', o.orderTime), " +
        "COUNT(o)) " +
        "FROM Order o WHERE o.restaurant.id = :restaurantId " +
        "GROUP BY FUNCTION('HOUR', o.orderTime) " +
        "ORDER BY FUNCTION('HOUR', o.orderTime) ASC")
    List<OrdersByHourResponse> findOrdersByHour(@Param("restaurantId") Long restaurantId);
}
