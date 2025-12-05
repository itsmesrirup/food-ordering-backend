package com.dass.foodordering.food_ordering_backend.dto.ai;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportedCategoryDTO {
    private String categoryName;
    private List<ImportedItemDTO> items = new ArrayList<>(); // Direct items
    private List<ImportedSubCategoryDTO> subCategories = new ArrayList<>(); // Nested subcategories
}