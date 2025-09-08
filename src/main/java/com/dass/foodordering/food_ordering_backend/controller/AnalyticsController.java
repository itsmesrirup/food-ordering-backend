package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.AnalyticsSummaryResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.TopSellingItemResponse;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.OrderItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/summary")
    public AnalyticsSummaryResponse getSummary() {
        Long restaurantId = getCurrentUserRestaurantId();
        return orderRepository.getAnalyticsSummary(restaurantId);
    }

    @GetMapping("/top-selling-items")
    public List<TopSellingItemResponse> getTopSellingItems() {
        Long restaurantId = getCurrentUserRestaurantId();
        // Here you could add pagination to get top 5, top 10 etc.
        return orderItemRepository.findTopSellingItems(restaurantId);
    }

    private Long getCurrentUserRestaurantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getRestaurant().getId();
    }
}