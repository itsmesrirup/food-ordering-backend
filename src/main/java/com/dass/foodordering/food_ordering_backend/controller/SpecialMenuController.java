package com.dass.foodordering.food_ordering_backend.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.SpecialMenu;
import com.dass.foodordering.food_ordering_backend.model.SpecialMenuItem;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.SpecialMenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.SpecialMenuRepository;

import lombok.Data;

@Data
@RestController
@RequestMapping("/api/special-menus")
public class SpecialMenuController {

    @Autowired private SpecialMenuRepository specialMenuRepository;
    @Autowired private SpecialMenuItemRepository specialMenuItemRepository;

    // --- DTOs for Requests ---
    @Data public static class SpecialMenuRequest {
        private String title;
        private String subtitle;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isActive;
    }
    @Data public static class SpecialMenuItemRequest {
        private String dayTitle;
        private String name;
        private String description;
    }

    // --- PUBLIC ENDPOINT ---
    @GetMapping("/restaurant/{restaurantId}/active")
    public ResponseEntity<SpecialMenu> getActiveSpecialMenuForRestaurant(@PathVariable Long restaurantId) {
        return specialMenuRepository
                .findFirstByRestaurantIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        restaurantId, LocalDate.now(), LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/my-restaurant")
    public List<SpecialMenu> getMyRestaurantSpecialMenus() {
        Long restaurantId = getCurrentUserRestaurantId();
        // This query fetches ALL special menus for the restaurant.
        List<SpecialMenu> menus = specialMenuRepository.findByRestaurantId(restaurantId);
        // Sorting them so the newest start dates are first.
        menus.sort(Comparator.comparing(SpecialMenu::getStartDate).reversed());
        return menus;
    }

    @PostMapping
    public SpecialMenu createSpecialMenu(@RequestBody SpecialMenuRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after the end date.");
        }
        User currentUser = getCurrentUser();
        SpecialMenu specialMenu = new SpecialMenu();
        specialMenu.setRestaurant(currentUser.getRestaurant());
        specialMenu.setTitle(request.getTitle());
        specialMenu.setSubtitle(request.getSubtitle());
        specialMenu.setStartDate(request.getStartDate());
        specialMenu.setEndDate(request.getEndDate());
        specialMenu.setActive(request.isActive());
        return specialMenuRepository.save(specialMenu);
    }

    @PostMapping("/{menuId}/items")
    public SpecialMenuItem addMenuItemToSpecialMenu(@PathVariable Long menuId, @RequestBody SpecialMenuItemRequest request) {
        SpecialMenu specialMenu = findMenuAndVerifyOwnership(menuId);
        SpecialMenuItem newItem = new SpecialMenuItem();
        newItem.setSpecialMenu(specialMenu);
        newItem.setDayTitle(request.getDayTitle());
        newItem.setName(request.getName());
        newItem.setDescription(request.getDescription());
        return specialMenuItemRepository.save(newItem);
    }

    @PutMapping("/{menuId}")
    public SpecialMenu updateSpecialMenu(@PathVariable Long menuId, @RequestBody SpecialMenuRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after the end date.");
        }
        
        // Reuse our security helper to find the menu and verify the owner
        SpecialMenu specialMenu = findMenuAndVerifyOwnership(menuId);
        
        specialMenu.setTitle(request.getTitle());
        specialMenu.setSubtitle(request.getSubtitle());
        specialMenu.setStartDate(request.getStartDate());
        specialMenu.setEndDate(request.getEndDate());
        specialMenu.setActive(request.isActive());
        
        return specialMenuRepository.save(specialMenu);
    }
    
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteSpecialMenu(@PathVariable Long menuId) {
        SpecialMenu specialMenu = findMenuAndVerifyOwnership(menuId);
        specialMenuRepository.delete(specialMenu);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}")
    public SpecialMenuItem updateSpecialMenuItem(
            @PathVariable Long itemId, 
            @RequestBody SpecialMenuItemRequest request) {
                
        // Find the item and verify the owner
        SpecialMenuItem itemToUpdate = specialMenuItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Special item not found"));
        findMenuAndVerifyOwnership(itemToUpdate.getSpecialMenu().getId()); // Re-use security helper

        // Update the fields
        itemToUpdate.setDayTitle(request.getDayTitle());
        itemToUpdate.setName(request.getName());
        itemToUpdate.setDescription(request.getDescription());

        return specialMenuItemRepository.save(itemToUpdate);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteSpecialMenuItem(@PathVariable Long itemId) {
        SpecialMenuItem item = specialMenuItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Special item not found"));
        findMenuAndVerifyOwnership(item.getSpecialMenu().getId());
        specialMenuItemRepository.delete(item);
        return ResponseEntity.noContent().build();
    }
    
    // --- Security Helpers ---
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private Long getCurrentUserRestaurantId() {
        return getCurrentUser().getRestaurant().getId();
    }

    private SpecialMenu findMenuAndVerifyOwnership(Long menuId) {
        User currentUser = getCurrentUser();
        SpecialMenu specialMenu = specialMenuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Special Menu not found"));
        if (!specialMenu.getRestaurant().getId().equals(currentUser.getRestaurant().getId())) {
            throw new ResourceNotFoundException("Special Menu not found");
        }
        return specialMenu;
    }
}