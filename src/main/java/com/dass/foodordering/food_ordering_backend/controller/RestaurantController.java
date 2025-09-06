package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.MenuItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.UpdateRestaurantRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantSettingsResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.dto.response.CategorizedMenuResponse;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;

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

    @Autowired
    private CategoryRepository categoryRepository;

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
    public List<CategorizedMenuResponse> getRestaurantMenu(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Fetch only the top-level categories for this restaurant
        return categoryRepository.findByRestaurantAndParentCategoryIsNull(restaurant).stream()
            .map(CategorizedMenuResponse::new)
            .collect(Collectors.toList());
    }

    // --- PROTECTED ADMIN ENDPOINTS ---
    
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

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id, 
            @RequestBody UpdateRestaurantRequest restaurantDetails) {
        
        // Security check: Make sure the logged-in user owns this restaurant
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getRestaurant().getId().equals(id)) {
            throw new ResourceNotFoundException("Restaurant not found for this user"); // Or AccessDeniedException
        }

        Restaurant restaurantToUpdate = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        
        restaurantToUpdate.setName(restaurantDetails.getName());
        restaurantToUpdate.setAddress(restaurantDetails.getAddress());
        restaurantToUpdate.setEmail(restaurantDetails.getEmail()); // Set the email
        
        // Update feature flags
        restaurantToUpdate.setReservationsEnabled(restaurantDetails.isReservationsEnabled());
        restaurantToUpdate.setQrCodeOrderingEnabled(restaurantDetails.isQrCodeOrderingEnabled());

        //Update the theme flags
        restaurantToUpdate.setThemePrimaryColor(restaurantDetails.getThemePrimaryColor());
        restaurantToUpdate.setThemeSecondaryColor(restaurantDetails.getThemeSecondaryColor());
        restaurantToUpdate.setLogoUrl(restaurantDetails.getLogoUrl());
        
        Restaurant updatedRestaurant = restaurantRepository.save(restaurantToUpdate);
        return ResponseEntity.ok(new RestaurantResponse(updatedRestaurant));
    }

    // Endpoint for a logged-in owner to get their own restaurant's details
    @GetMapping("/me")
    public ResponseEntity<RestaurantSettingsResponse> getMyRestaurant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Restaurant myRestaurant = currentUser.getRestaurant();
        
        if (myRestaurant == null) {
            throw new ResourceNotFoundException("No restaurant associated with this user.");
        }
        
        return ResponseEntity.ok(new RestaurantSettingsResponse(myRestaurant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurantRepository.delete(restaurant);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
