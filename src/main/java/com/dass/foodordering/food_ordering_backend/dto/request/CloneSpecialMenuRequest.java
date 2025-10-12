package com.dass.foodordering.food_ordering_backend.dto.request;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CloneSpecialMenuRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}