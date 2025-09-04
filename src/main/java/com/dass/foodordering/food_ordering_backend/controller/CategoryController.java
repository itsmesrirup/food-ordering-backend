package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.CategoryResponse;
import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;

    // Endpoint for an admin to get all categories for their restaurant
    @GetMapping("/by-restaurant")
    public List<CategoryResponse> getCategoriesByRestaurant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Restaurant restaurant = currentUser.getRestaurant();
        
        return categoryRepository.findByRestaurantAndParentCategoryIsNull(restaurant).stream()
            .map(CategoryResponse::new)
            .collect(Collectors.toList());
    }

    // You would add POST, PUT, DELETE methods here for managing categories
}