package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerName;
    private List<OrderItemRequest> items; // Changed from menuItemIds
    private Long customerId;
}