package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedCategoryDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedItemDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.ImportedSubCategoryDTO;
import com.dass.foodordering.food_ordering_backend.dto.ai.MenuImportResult;
import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.service.AiMenuParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/menu-import")
public class MenuImportController {

    @Autowired private AiMenuParserService aiService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MenuItemRepository menuItemRepository;

    // Step 1: Upload Image & Get JSON Preview
    @PostMapping("/parse")
    public ResponseEntity<MenuImportResult> parseMenu(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(aiService.parseMenuImage(file));
    }

    // Step 2: Save Verified JSON to Database
    @PostMapping("/save")
    public ResponseEntity<Void> saveImportedMenu(@RequestBody MenuImportResult verifiedData) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Restaurant restaurant = currentUser.getRestaurant();

        for (ImportedCategoryDTO catDTO : verifiedData.getCategories()) {
            
            // 1. Find existing Main Category OR Create new one
            Category mainCategory = categoryRepository
                .findByNameAndRestaurantAndParentCategoryIsNull(catDTO.getCategoryName(), restaurant)
                .orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(catDTO.getCategoryName());
                    newCat.setRestaurant(restaurant);
                    return categoryRepository.save(newCat);
                });

            // 2. Save Direct Items
            if (catDTO.getItems() != null) {
                saveItems(catDTO.getItems(), mainCategory, restaurant);
            }

            // 3. Process Subcategories
            if (catDTO.getSubCategories() != null) {
                for (ImportedSubCategoryDTO subDTO : catDTO.getSubCategories()) {
                    
                    // Find existing Subcategory under this parent OR Create new one
                    Category subCategory = categoryRepository
                        .findByNameAndParentCategory(subDTO.getName(), mainCategory)
                        .orElseGet(() -> {
                            Category newSub = new Category();
                            newSub.setName(subDTO.getName());
                            newSub.setRestaurant(restaurant);
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
            item.setRestaurant(restaurant);
            item.setAvailable(true);
            menuItemRepository.save(item);
        }
    }
}