package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegisterRequest {
    private String name;
    private String email;
    private String password;
    private Long restaurantId;
    // You can add other optional fields like 'phone' here if you want to
    // capture them during sign-up.
}