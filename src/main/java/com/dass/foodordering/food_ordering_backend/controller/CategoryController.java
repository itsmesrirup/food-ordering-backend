package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.CategoryResponse;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CategoryRepository;

import lombok.Data;

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

    @Data
    public static class CreateCategoryRequest {
        private String name;
        private Long parentCategoryId; // Optional, for creating subcategories
    }
    @Data
    public static class UpdateCategoryRequest {
        private String name;
    }
    
    // ✅ NEW ENDPOINT to create a category
    @PostMapping
    public CategoryResponse createCategory(@RequestBody CreateCategoryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Restaurant restaurant = currentUser.getRestaurant();
        
        Category newCategory = new Category();
        newCategory.setName(request.getName());
        newCategory.setRestaurant(restaurant);
        
        // Handle subcategory creation
        if (request.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.getParentCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            // Security check: ensure parent belongs to the same restaurant
            if (!parent.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("Invalid parent category");
            }
            newCategory.setParentCategory(parent);
        }
        
        Category savedCategory = categoryRepository.save(newCategory);
        return new CategoryResponse(savedCategory);
    }

    // PUT (Update) an existing category's name
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        Category category = findCategoryAndVerifyOwnership(id);
        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(new CategoryResponse(updatedCategory));
    }

    // DELETE a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = findCategoryAndVerifyOwnership(id);
        
        // Basic check: prevent deleting a category that still has items or subcategories
        if (!category.getMenuItems().isEmpty() || !category.getSubCategories().isEmpty()) {
            throw new BadRequestException("Cannot delete a category that contains menu items or subcategories.");
        }
        
        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    // Helper method to reduce code duplication and enforce security
    private Category findCategoryAndVerifyOwnership(Long categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long userRestaurantId = currentUser.getRestaurant().getId();

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getRestaurant().getId().equals(userRestaurantId)) {
            // Do not reveal that the category exists but belongs to another user
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }
}