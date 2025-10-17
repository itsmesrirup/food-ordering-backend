package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrdersByHourResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.SalesByPeriodResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrdersByHourResponseProjection;
import com.dass.foodordering.food_ordering_backend.service.FeatureService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired private FeatureService featureService;

    // --- A reusable helper method to get the user and verify their plan ---
    private Restaurant checkAnalyticsAccessAndGetRestaurant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Restaurant restaurant = currentUser.getRestaurant();
        
        // --- THIS IS THE GUARD ---
        if (!featureService.isFeatureAvailable(restaurant, "ANALYTICS")) {
            throw new AccessDeniedException("Access to Analytics is denied. Upgrade to the PREMIUM plan.");
        }
        return restaurant;
    }


    @GetMapping("/summary")
    public AnalyticsSummaryResponse getSummary() {
        Restaurant restaurant = checkAnalyticsAccessAndGetRestaurant();
        return orderRepository.getAnalyticsSummary(restaurant.getId());
    }

    @GetMapping("/top-selling-items")
    public List<TopSellingItemResponse> getTopSellingItems() {
        Restaurant restaurant = checkAnalyticsAccessAndGetRestaurant();
        return orderItemRepository.findTopSellingItems(restaurant.getId());
    }

    @GetMapping("/sales-over-time")
    public List<SalesByPeriodResponse> getSalesOverTime() {
        Restaurant restaurant = checkAnalyticsAccessAndGetRestaurant();
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        return orderRepository.findSalesByDay(restaurant.getId(), startDate);
    }

    @GetMapping("/orders-by-hour")
    public List<OrdersByHourResponse> getOrdersByHour() {
        Restaurant restaurant = checkAnalyticsAccessAndGetRestaurant();
        
        // Call the new native query method
        List<OrdersByHourResponseProjection> results = orderRepository.findOrdersByHourNative(restaurant.getId());
        
        // Manually map the projection results to our DTO
        return results.stream()
                .map(proj -> new OrdersByHourResponse(proj.getHour(), proj.getOrderCount()))
                .collect(Collectors.toList());
    }

    @GetMapping("/recommendations/{menuItemId}")
    public List<MenuItemResponse> getRecommendations(@PathVariable Long menuItemId) {
        // Find the IDs of frequently co-purchased items
        List<Long> recommendedIds = orderItemRepository.findFrequentlyBoughtWith(menuItemId)
            .stream().limit(3).collect(Collectors.toList()); // Limit to top 3

        if (recommendedIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch the full MenuItem details for those IDs
        return menuItemRepository.findAllById(recommendedIds).stream()
            .map(MenuItemResponse::new)
            .collect(Collectors.toList());
    }
}