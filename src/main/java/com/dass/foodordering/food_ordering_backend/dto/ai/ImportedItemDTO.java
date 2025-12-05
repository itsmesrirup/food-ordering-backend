package com.dass.foodordering.food_ordering_backend.dto.ai;

import lombok.Data;

@Data
public class ImportedItemDTO {
    private String name;
    private String description;
    private Double price;
}