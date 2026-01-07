package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Category;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentCategoryId;
    private List<CategoryResponse> subCategories;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        if (category.getParentCategory() != null) {
            this.parentCategoryId = category.getParentCategory().getId();
        }
        if (category.getSubCategories() != null) {
            this.subCategories = category.getSubCategories().stream()
                    .filter(sub -> !sub.isDeleted()) // --- ADDED FILTER ---
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());
        }
    }
}