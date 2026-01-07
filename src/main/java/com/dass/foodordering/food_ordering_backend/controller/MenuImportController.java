package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedCategoryDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedItemDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedSubCategoryDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.MenuImportResult;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.service.AiMenuParserService;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/menu-import")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class MenuImportController {

    @Autowired private AiMenuParserService aiService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private RestaurantRepository restaurantRepository;

    // Wrapper DTO to handle the new optional field
    @Data
    public static class ImportMenuRequest {
        private MenuImportResult menuData;
        private Long targetRestaurantId; // Optional: Only for Super Admin
    }

    // Step 1: Upload Image & Get JSON Preview
    @PostMapping("/parse")
    public ResponseEntity<MenuImportResult> parseMenu(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(aiService.parseMenuImage(file));
    }

    // Step 2: Save Verified JSON to Database
    @PostMapping("/save")
    public ResponseEntity<Void> saveImportedMenu(@RequestBody ImportMenuRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Restaurant restaurant;

        // --- NEW LOGIC: DETERMINE TARGET RESTAURANT ---
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            if (request.getTargetRestaurantId() == null) {
                throw new IllegalArgumentException("Super Admin must provide a target restaurant ID.");
            }
            restaurant = restaurantRepository.findEvenInactiveById(request.getTargetRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target restaurant not found."));
        } else {
            // Regular admin always imports to their own restaurant
            restaurant = currentUser.getRestaurant();
            // Optional: Prevent regular admins from trying to hack another ID
            if (request.getTargetRestaurantId() != null && !request.getTargetRestaurantId().equals(restaurant.getId())) {
                 throw new AccessDeniedException("You can only import menus for your own restaurant.");
            }
        }

        // --- CORE SAVE LOGIC (Unchanged, just uses the 'restaurant' variable determined above) ---
        MenuImportResult verifiedData = request.getMenuData();
        
        for (ImportedCategoryDTO catDTO : verifiedData.getCategories()) {
            
            // 1. Find existing Main Category OR Create new one
            Category mainCategory = categoryRepository
                .findByNameAndRestaurantAndParentCategoryIsNull(catDTO.getCategoryName(), restaurant)
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(catDTO.getCategoryName());
                    newCat.setRestaurant(restaurant); // Use the target restaurant
                    return categoryRepository.save(newCat);
                });

            // 2. Save Direct Items
            if (catDTO.getItems() != null) {
                saveItems(catDTO.getItems(), mainCategory, restaurant);
            }

            // 3. Process Subcategories
            if (catDTO.getSubCategories() != null) {
                for (ImportedSubCategoryDTO subDTO : catDTO.getSubCategories()) {
                    
                    Category subCategory = categoryRepository
                        .findByNameAndParentCategory(subDTO.getName(), mainCategory)
                        .orElseGet(() -> {
                            Category newSub = new Category();
                            newSub.setName(subDTO.getName());
                            newSub.setRestaurant(restaurant); // Use the target restaurant
                            newSub.setParentCategory(mainCategory);
                            return categoryRepository.save(newSub);
                        });

                    if (subDTO.getItems() != null) {
                        saveItems(subDTO.getItems(), subCategory, restaurant);
                    }
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    private void saveItems(List<ImportedItemDTO> itemDTOs, Category category, Restaurant restaurant) {
        for (ImportedItemDTO itemDTO : itemDTOs) {
            MenuItem item = new MenuItem();
            item.setName(itemDTO.getName());
            item.setDescription(itemDTO.getDescription());
            item.setPrice(itemDTO.getPrice() != null ? itemDTO.getPrice() : 0.0);
            item.setCategory(category);
            item.setRestaurant(restaurant); // Use the target restaurant
            item.setAvailable(true);
            menuItemRepository.save(item);
        }
    }
}