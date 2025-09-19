package com.dass.foodordering.food_ordering_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAuthResponse {
    private Long customerId;
    private String email;
    private String message;
    private String token; // <-- JWT token for frontend authentication
}