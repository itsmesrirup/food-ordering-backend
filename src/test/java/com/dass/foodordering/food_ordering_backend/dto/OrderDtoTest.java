package com.dass.foodordering.food_ordering_backend.dto;

import com.dass.foodordering.food_ordering_backend.dto.request.OrderRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderResponse;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

class OrderDtoTest {

    @Test
    void testOrderRequestWithGuestFields() {
        // Test OrderRequest with guest information
        OrderRequest request = new OrderRequest();
        request.setGuestName("Alice Johnson");
        request.setGuestEmail("alice@example.com");
        request.setItems(new ArrayList<>());
        request.setCustomerId(null); // No customer ID for guest orders
        
        assertEquals("Alice Johnson", request.getGuestName());
        assertEquals("alice@example.com", request.getGuestEmail());
        assertNull(request.getCustomerId());
    }

    @Test
    void testOrderRequestWithCustomer() {
        // Test OrderRequest with customer information
        OrderRequest request = new OrderRequest();
        request.setCustomerId(123L);
        request.setItems(new ArrayList<>());
        // Guest fields should be null/empty for customer orders
        
        assertEquals(Long.valueOf(123), request.getCustomerId());
        assertNull(request.getGuestName());
        assertNull(request.getGuestEmail());
    }

    @Test
    void testOrderResponseWithGuestOrder() {
        // Test OrderResponse for guest order
        Order order = new Order();
        order.setId(1L);
        order.setGuestName("Bob Wilson");
        order.setGuestEmail("bob@example.com");
        order.setTotalPrice(19.99);
        order.setOrderItems(new ArrayList<>());
        
        OrderResponse response = new OrderResponse(order);
        
        assertEquals(Long.valueOf(1), response.getId());
        assertEquals("Bob Wilson", response.getGuestName());
        assertEquals("bob@example.com", response.getGuestEmail());
        assertEquals(Double.valueOf(19.99), response.getTotalPrice());
        assertNull(response.getCustomerId());
    }

    @Test
    void testOrderResponseWithCustomerOrder() {
        // Test OrderResponse for customer order
        Customer customer = new Customer();
        customer.setId(456L);
        customer.setName("Carol Davis");
        
        Order order = new Order();
        order.setId(2L);
        order.setCustomer(customer);
        order.setTotalPrice(29.99);
        order.setOrderItems(new ArrayList<>());
        
        OrderResponse response = new OrderResponse(order);
        
        assertEquals(Long.valueOf(2), response.getId());
        assertEquals(Long.valueOf(456), response.getCustomerId());
        assertEquals(Double.valueOf(29.99), response.getTotalPrice());
        assertNull(response.getGuestName());
        assertNull(response.getGuestEmail());
    }
}