package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.OrderItem;
import lombok.Data;

@Data
public class OrderItemResponse {
    private Long menuItemId;
    private String name;
    private int quantity;
    private double price;

    public OrderItemResponse(OrderItem orderItem) {
        this.menuItemId = orderItem.getMenuItem().getId();
        this.name = orderItem.getMenuItem().getName();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getMenuItem().getPrice();
    }
}
