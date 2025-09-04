package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Category;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategorizedMenuResponse {
    private Long id;
    private String name;
    private List<MenuItemResponse> menuItems;
    private List<CategorizedMenuResponse> subCategories;

    // This constructor recursively builds the categorized menu structure
    public CategorizedMenuResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();

        // Map the menu items that belong directly to this category
        if (category.getMenuItems() != null) {
            this.menuItems = category.getMenuItems().stream()
                    .map(MenuItemResponse::new)
                    .collect(Collectors.toList());
        }

        // Recursively map the subcategories
        if (category.getSubCategories() != null) {
            this.subCategories = category.getSubCategories().stream()
                    .map(CategorizedMenuResponse::new)
                    .collect(Collectors.toList());
        }
    }
}