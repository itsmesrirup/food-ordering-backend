package com.dass.foodordering.food_ordering_backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerName; // Kept for backward compatibility, but may be replaced by guestName
    private List<OrderItemRequest> items; // Changed from menuItemIds
    private Long customerId; // Optional for guest orders
    private String tableNumber;
    
    // Guest order fields
    private String guestName;
    private String guestEmail;
}