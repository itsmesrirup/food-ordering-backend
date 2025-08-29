package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.MenuItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.MenuItemResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    // This endpoint is useful for a global search, but not for managing a specific menu
    @GetMapping
    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll()
                .stream()
                .map(MenuItemResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        return menuItemRepository.findById(id)
                .map(menuItem -> ResponseEntity.ok(new MenuItemResponse(menuItem)))
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + id));
    }

    // This endpoint is used by the RestaurantController's menu management
    // POST /api/restaurants/{restaurantId}/menu-items is more RESTful, but we will use this for now
    @PostMapping
    public MenuItemResponse createMenuItem(@RequestBody MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId()));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setDescription(request.getDescription());
        menuItem.setRestaurant(restaurant);

        MenuItem saved = menuItemRepository.save(menuItem);
        return new MenuItemResponse(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequest menuItemDetails) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + id));

        // You can't change the restaurant an item belongs to, so we don't update it.
        menuItem.setName(menuItemDetails.getName());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setDescription(menuItemDetails.getDescription());

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return ResponseEntity.ok(new MenuItemResponse(updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + id));
        
        menuItemRepository.delete(menuItem);
        return ResponseEntity.noContent().build();
    }
}