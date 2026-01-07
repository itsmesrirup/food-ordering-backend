package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String newPassword;
}