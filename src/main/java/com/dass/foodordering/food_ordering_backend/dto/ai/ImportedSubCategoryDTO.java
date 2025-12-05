package com.dass.foodordering.food_ordering_backend.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class ImportedSubCategoryDTO {
    private String name;
    private List<ImportedItemDTO> items;
}