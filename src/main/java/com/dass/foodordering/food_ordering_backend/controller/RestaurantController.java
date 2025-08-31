package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.MenuItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(RestaurantResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return restaurantRepository.findById(id)
                .map(restaurant -> ResponseEntity.ok(new RestaurantResponse(restaurant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{restaurantId}/menu")
    public List<MenuItemResponse> getRestaurantMenu(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        return restaurant.getMenuItems().stream()
                .map(MenuItemResponse::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public RestaurantResponse createRestaurant(@RequestBody Restaurant restaurant) {
        return new RestaurantResponse(restaurantRepository.save(restaurant));
    }

    // ✅ New endpoint: Add MenuItem directly under Restaurant
    @PostMapping("/{restaurantId}/menu-items")
    public MenuItemResponse addMenuItemToRestaurant(
            @PathVariable Long restaurantId,
            @RequestBody MenuItemRequest request) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setRestaurant(restaurant);

        MenuItem saved = menuItemRepository.save(menuItem);
        return new MenuItemResponse(saved);
    }

    @Data
    public static class UpdateRestaurantRequest {
        private String name;
        private String address;
        private String email;
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id, 
            @RequestBody UpdateRestaurantRequest restaurantDetails) {
        
        // Security check: Make sure the logged-in user owns this restaurant
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getRestaurant().getId().equals(id)) {
            throw new ResourceNotFoundException("Restaurant not found"); // Or AccessDeniedException
        }

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        restaurant.setName(restaurantDetails.getName());
        restaurant.setAddress(restaurantDetails.getAddress());
        restaurant.setEmail(restaurantDetails.getEmail()); // Set the email
        
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return ResponseEntity.ok(new RestaurantResponse(updatedRestaurant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurantRepository.delete(restaurant);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
