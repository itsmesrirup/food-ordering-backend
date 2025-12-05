package com.dass.foodordering.food_ordering_backend.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class MenuImportResult {
    private List<ImportedCategoryDTO> categories;
}