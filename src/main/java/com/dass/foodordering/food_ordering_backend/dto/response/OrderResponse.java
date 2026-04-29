package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.OrderStatus;

import java.time.LocalDateTime;
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
    private LocalDateTime pickupTime;
    private String paymentIntentId;
    private Long orderNumber; // The customer-facing number
    private String restaurantSlug;
    private String source;

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
        this.pickupTime = order.getPickupTime();
        this.paymentIntentId = order.getPaymentIntentId();
        this.orderNumber = order.getRestaurantOrderSequence();
        if (order.getRestaurant() != null) {
            this.restaurantSlug = order.getRestaurant().getSlug();
        }
        this.source = order.getSource() != null ? order.getSource().name() : "ONLINE";
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
    public LocalDateTime getPickupTime() {
        return pickupTime;
    }
    public String getPaymentIntentId() { return paymentIntentId; }
    public Long getOrderNumber() {
        return orderNumber;
    }
    public String getRestaurantSlug() {
        return restaurantSlug;
    }
    public String getSource() { return source; }
}
