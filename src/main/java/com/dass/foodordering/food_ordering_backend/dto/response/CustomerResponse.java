package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Customer;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private List<OrderResponse> orders;

    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.orders = customer.getOrders().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<OrderResponse> getOrders() { return orders; }
}
