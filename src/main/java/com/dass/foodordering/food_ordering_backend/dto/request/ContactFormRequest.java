package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class ContactFormRequest {
    private String name;
    private String email;
    private String restaurantName;
    private String message;
}