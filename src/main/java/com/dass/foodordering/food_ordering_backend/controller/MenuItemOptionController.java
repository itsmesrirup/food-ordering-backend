package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.MenuItemOption;
import com.dass.foodordering.food_ordering_backend.model.MenuItemOptionChoice;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemOptionChoiceRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemOptionRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MenuItemOptionController {
    
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private MenuItemOptionRepository menuItemOptionRepository;
    @Autowired private MenuItemOptionChoiceRepository choiceRepository;

    // --- DTOs for Requests ---
    @Data
    public static class OptionRequest {
        private String name;
        private int minChoices;
        private int maxChoices;
    }
    @Data
    public static class ChoiceRequest {
        private String name;
        private double priceAdjustment;
    }

    // --- Option Group (e.g., "Entrée au choix") Endpoints ---

    @PostMapping("/menu-items/{menuItemId}/options")
    public MenuItemOption createOption(@PathVariable Long menuItemId, @RequestBody OptionRequest request) {
        MenuItem menuItem = findMenuItemAndVerifyOwnership(menuItemId);
        MenuItemOption option = new MenuItemOption();
        option.setMenuItem(menuItem);
        option.setName(request.getName());
        option.setMinChoices(request.getMinChoices());
        option.setMaxChoices(request.getMaxChoices());
        return menuItemOptionRepository.save(option);
    }
    
    @PutMapping("/menu-item-options/{optionId}")
    public MenuItemOption updateOption(@PathVariable Long optionId, @RequestBody OptionRequest request) {
        MenuItemOption option = findOptionAndVerifyOwnership(optionId);
        option.setName(request.getName());
        option.setMinChoices(request.getMinChoices());
        option.setMaxChoices(request.getMaxChoices());
        return menuItemOptionRepository.save(option);
    }

    @DeleteMapping("/menu-item-options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        MenuItemOption option = findOptionAndVerifyOwnership(optionId);
        menuItemOptionRepository.delete(option);
        return ResponseEntity.noContent().build();
    }


    // --- Individual Choice (e.g., "Samosa") Endpoints ---

    @PostMapping("/menu-item-options/{optionId}/choices")
    public MenuItemOptionChoice createChoice(@PathVariable Long optionId, @RequestBody ChoiceRequest request) {
        MenuItemOption option = findOptionAndVerifyOwnership(optionId);
        MenuItemOptionChoice choice = new MenuItemOptionChoice();
        choice.setMenuItemOption(option);
        choice.setName(request.getName());
        choice.setPriceAdjustment(request.getPriceAdjustment());
        return choiceRepository.save(choice);
    }

    @PutMapping("/menu-item-option-choices/{choiceId}")
    public MenuItemOptionChoice updateChoice(@PathVariable Long choiceId, @RequestBody ChoiceRequest request) {
        MenuItemOptionChoice choice = findChoiceAndVerifyOwnership(choiceId);
        choice.setName(request.getName());
        choice.setPriceAdjustment(request.getPriceAdjustment());
        return choiceRepository.save(choice);
    }

    @DeleteMapping("/menu-item-option-choices/{choiceId}")
    public ResponseEntity<Void> deleteChoice(@PathVariable Long choiceId) {
        MenuItemOptionChoice choice = findChoiceAndVerifyOwnership(choiceId);
        choiceRepository.delete(choice);
        return ResponseEntity.noContent().build();
    }


    // --- Security Helper Methods ---
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
    private MenuItem findMenuItemAndVerifyOwnership(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(() -> new ResourceNotFoundException("Menu Item not found"));
        if (!menuItem.getRestaurant().getId().equals(getCurrentUser().getRestaurant().getId())) {
            throw new ResourceNotFoundException("Menu Item not found");
        }
        return menuItem;
    }
    private MenuItemOption findOptionAndVerifyOwnership(Long optionId) {
        MenuItemOption option = menuItemOptionRepository.findById(optionId).orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        if (!option.getMenuItem().getRestaurant().getId().equals(getCurrentUser().getRestaurant().getId())) {
            throw new ResourceNotFoundException("Option not found");
        }
        return option;
    }
    private MenuItemOptionChoice findChoiceAndVerifyOwnership(Long choiceId) {
        MenuItemOptionChoice choice = choiceRepository.findById(choiceId).orElseThrow(() -> new ResourceNotFoundException("Choice not found"));
        if (!choice.getMenuItemOption().getMenuItem().getRestaurant().getId().equals(getCurrentUser().getRestaurant().getId())) {
            throw new ResourceNotFoundException("Choice not found");
        }
        return choice;
    }
}