package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.MenuItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.UpdateRestaurantRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantSettingsResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantSummaryResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.dto.response.CategorizedMenuResponse;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;
import com.dass.foodordering.food_ordering_backend.service.FeatureService;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private FeatureService featureService;
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(RestaurantResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return restaurantRepository.findByIdAndActiveTrue(id)
                .map(restaurant -> ResponseEntity.ok(new RestaurantResponse(restaurant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{restaurantId}/menu")
    public List<CategorizedMenuResponse> getRestaurantMenu(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(restaurantId)
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

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(restaurantId)
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
        if (featureService.isFeatureAvailable(restaurantToUpdate, "RESERVATIONS")) {
            restaurantToUpdate.setReservationsEnabled(restaurantDetails.isReservationsEnabled());
        }
        if (featureService.isFeatureAvailable(restaurantToUpdate, "QR_ORDERING")) {
            restaurantToUpdate.setQrCodeOrderingEnabled(restaurantDetails.isQrCodeOrderingEnabled());
        }
        if (featureService.isFeatureAvailable(restaurantToUpdate, "RECOMMENDATIONS")) {
            restaurantToUpdate.setRecommendationsEnabled(restaurantDetails.isRecommendationsEnabled());
        }

        //Update the theme flags
        restaurantToUpdate.setUseDarkTheme(restaurantDetails.isUseDarkTheme());
        restaurantToUpdate.setLogoUrl(restaurantDetails.getLogoUrl());
        restaurantToUpdate.setHeroImageUrl(restaurantDetails.getHeroImageUrl());

        // --- Save the new website content fields ---
        restaurantToUpdate.setAboutUsText(restaurantDetails.getAboutUsText());
        restaurantToUpdate.setPhoneNumber(restaurantDetails.getPhoneNumber());
        restaurantToUpdate.setOpeningHours(restaurantDetails.getOpeningHours());
        restaurantToUpdate.setGoogleMapsUrl(restaurantDetails.getGoogleMapsUrl());
        restaurantToUpdate.setSlug(restaurantDetails.getSlug());
        restaurantToUpdate.setMetaTitle(restaurantDetails.getMetaTitle());
        restaurantToUpdate.setMetaDescription(restaurantDetails.getMetaDescription());
        
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
        
        Set<String> availableFeatures = featureService.getAvailableFeaturesForRestaurant(myRestaurant);
        
        return ResponseEntity.ok(new RestaurantSettingsResponse(myRestaurant, availableFeatures));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        // We need a special repository method to find even inactive restaurants
        Restaurant restaurant = restaurantRepository.findEvenInactiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
        
        // Optional: Also deactivate all associated admin user accounts
        // userRepository.deactivateUsersByRestaurantId(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public List<RestaurantSummaryResponse> getAllRestaurantsForSuperAdmin() {
        // We need a new repository method that ignores the @Where clause
        return restaurantRepository.findAllEvenInactive().stream()
            .map(RestaurantSummaryResponse::new)
            .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/reactivate")
    @Transactional
    public ResponseEntity<Void> reactivateRestaurant(@PathVariable Long id) {
        // We use our special method to find the restaurant even if it's inactive
        Restaurant restaurant = restaurantRepository.findEvenInactiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        restaurant.setActive(true);
        restaurantRepository.save(restaurant);
        
        // You could also add logic here to reactivate its users if they were deactivated.

        return ResponseEntity.noContent().build();
    }

    // --- New endpoint for Super Admin to change a restaurant's plan ---
    @Data public static class UpdatePlanRequest { private SubscriptionPlan plan; }

    @PatchMapping("/{id}/plan")
    // Add security check to ensure only SUPER_ADMIN can call this
    @PreAuthorize("hasRole('SUPER_ADMIN')") 
    public ResponseEntity<Void> updateRestaurantPlan(@PathVariable Long id, @RequestBody UpdatePlanRequest request) {
        Restaurant restaurant = restaurantRepository.findEvenInactiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        SubscriptionPlan newPlan = request.getPlan();
        restaurant.setPlan(newPlan);
        
        // --- ADDED: Logic to disable features on downgrade ---
        // Get the set of features available for the new plan.
        Set<String> newAvailableFeatures = featureService.getFeaturesForPlan(newPlan);

        // If "RESERVATIONS" is no longer available, turn it off.
        if (!newAvailableFeatures.contains("RESERVATIONS")) {
            restaurant.setReservationsEnabled(false);
        }
        // If "QR_ORDERING" is no longer available, turn it off.
        if (!newAvailableFeatures.contains("QR_ORDERING")) {
            restaurant.setQrCodeOrderingEnabled(false);
        }
        // If "RECOMMENDATIONS" is no longer available, turn it off.
        if (!newAvailableFeatures.contains("RECOMMENDATIONS")) {
            restaurant.setRecommendationsEnabled(false);
        }
        // Add more checks here for future features like WEBSITE_BUILDER, etc.
        
        restaurantRepository.save(restaurant);
        
        return ResponseEntity.noContent().build();
    }

    // --- ADDED: Endpoint to update the Payment Model ---
    @Data public static class UpdateModelRequest { private PaymentModel paymentModel; }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/model")
    public ResponseEntity<Void> updateRestaurantModel(@PathVariable Long id, @RequestBody UpdateModelRequest request) {
        Restaurant restaurant = restaurantRepository.findEvenInactiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurant.setPaymentModel(request.getPaymentModel());
        restaurantRepository.save(restaurant);
        return ResponseEntity.noContent().build();
    }
    
    // --- ADDED: Endpoint to update the Commission Rate ---
    @Data public static class UpdateCommissionRequest { private BigDecimal commissionRate; }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/commission")
    public ResponseEntity<Void> updateRestaurantCommission(@PathVariable Long id, @RequestBody UpdateCommissionRequest request) {
        Restaurant restaurant = restaurantRepository.findEvenInactiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurant.setCommissionRate(request.getCommissionRate());
        restaurantRepository.save(restaurant);
        return ResponseEntity.noContent().build();
    }
}
