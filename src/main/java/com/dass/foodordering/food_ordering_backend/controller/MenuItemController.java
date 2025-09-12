package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.MenuItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

        @Autowired
        private MenuItemRepository menuItemRepository;

        @Autowired
        private RestaurantRepository restaurantRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        // This endpoint is useful for a global search, but not for managing a specific
        // menu
        @GetMapping
        public List<MenuItemResponse> getAllMenuItems() {
                return menuItemRepository.findAll()
                                .stream()
                                .map(MenuItemResponse::new)
                                .collect(Collectors.toList());
        }

        // For the modal to fetch the most up-to-date item data
        @GetMapping("/{id}")
        public MenuItemResponse getMenuItemById(@PathVariable Long id) {
        MenuItem menuItem = findMenuItemAndVerifyOwnership(id); // Reuse our security helper
        return new MenuItemResponse(menuItem);
        }

        @GetMapping("/by-restaurant")
        public List<MenuItemResponse> getMenuItemsByRestaurant() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = (User) authentication.getPrincipal();
                Long restaurantId = currentUser.getRestaurant().getId();

                return menuItemRepository.findByRestaurantId(restaurantId).stream()
                                .map(MenuItemResponse::new)
                                .collect(Collectors.toList());
        }

        // This endpoint is used by the RestaurantController's menu management
        // POST /api/restaurants/{restaurantId}/menu-items is more RESTful, but we will
        // use this for now
        @PostMapping
        public MenuItemResponse createMenuItem(@RequestBody MenuItemRequest request) {
                Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + request.getRestaurantId()));

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                MenuItem menuItem = new MenuItem();
                menuItem.setName(request.getName());
                menuItem.setPrice(request.getPrice());
                menuItem.setDescription(request.getDescription());
                menuItem.setRestaurant(restaurant);
                menuItem.setCategory(category); // Set the category
                menuItem.setBundle(request.isBundle());

                MenuItem saved = menuItemRepository.save(menuItem);
                return new MenuItemResponse(saved);
        }

        @PutMapping("/{id}")
        public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id,
                        @RequestBody MenuItemRequest menuItemDetails) {
                MenuItem menuItem = findMenuItemAndVerifyOwnership(id);

                Category category = categoryRepository.findById(menuItemDetails.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                // You can't change the restaurant an item belongs to, so we don't update it.
                menuItem.setName(menuItemDetails.getName());
                menuItem.setPrice(menuItemDetails.getPrice());
                menuItem.setDescription(menuItemDetails.getDescription());
                menuItem.setCategory(category); // Update the category
                menuItem.setBundle(menuItemDetails.isBundle());

                MenuItem updatedItem = menuItemRepository.save(menuItem);
                return ResponseEntity.ok(new MenuItemResponse(updatedItem));
        }

        // DTO for the request body
        @Data
        public static class AvailabilityRequest {
                private boolean available;
        }

        // ENDPOINT
        @PatchMapping("/{id}/availability")
        public ResponseEntity<MenuItemResponse> updateAvailability(
                        @PathVariable Long id,
                        @RequestBody AvailabilityRequest request) {

                // Reuse the security logic from your other admin endpoints to find the item and
                // verify ownership
                MenuItem menuItem = findMenuItemAndVerifyOwnership(id); // You will need to create this helper method

                menuItem.setAvailable(request.isAvailable()); // Lombok generates isAvailable() for a boolean 'available'
                MenuItem updatedItem = menuItemRepository.save(menuItem);
                return ResponseEntity.ok(new MenuItemResponse(updatedItem));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
                MenuItem menuItem = findMenuItemAndVerifyOwnership(id);

                menuItemRepository.delete(menuItem);
                return ResponseEntity.noContent().build();
        }

        private MenuItem findMenuItemAndVerifyOwnership(Long menuItemId) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = (User) authentication.getPrincipal();
                Long userRestaurantId = currentUser.getRestaurant().getId();

                MenuItem menuItem = menuItemRepository.findById(menuItemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

                if (!menuItem.getRestaurant().getId().equals(userRestaurantId)) {
                        throw new ResourceNotFoundException("Menu item not found");
                }
                return menuItem;
        }
}