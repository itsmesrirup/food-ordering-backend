package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private Long customerId;
    private Long restaurantId;   // ✅ add this
    //private List<MenuItemResponse> menuItems;
    private List<OrderItemResponse> items; // Changed from menuItems
    private double totalPrice;
    private String tableNumber;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        this.restaurantId = order.getRestaurant() != null ? order.getRestaurant().getId() : null; // ✅ map restaurant
        this.totalPrice = order.getTotalPrice();
        this.items = order.getOrderItems().stream() // Use getOrderItems()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
        this.tableNumber = order.getTableNumber();
    }

    // Getters
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public Long getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; } // ✅ new getter
    //public List<MenuItemResponse> getMenuItems() { return menuItems; }
    public List<OrderItemResponse> getItems() { return items; }
    public Double getTotalPrice() { return totalPrice; }
    public String getTableNumber() { return tableNumber; }
}
