package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class CustomerRequest {
    private String name;
    private String email;
    private String phone;
}
