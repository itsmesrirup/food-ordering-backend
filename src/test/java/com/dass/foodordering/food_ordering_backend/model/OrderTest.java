package com.dass.foodordering.food_ordering_backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testGuestOrderCreation() {
        // Test creating a guest order without customer
        Order order = new Order();
        order.setGuestName("John Doe");
        order.setGuestEmail("john.doe@example.com");
        order.setTotalPrice(25.99);
        
        // Verify guest fields are set
        assertEquals("John Doe", order.getGuestName());
        assertEquals("john.doe@example.com", order.getGuestEmail());
        assertEquals(25.99, order.getTotalPrice());
        
        // Verify customer is null for guest orders
        assertNull(order.getCustomer());
    }

    @Test
    void testCustomerOrderCreation() {
        // Test creating an order with a customer
        Customer customer = new Customer();
        customer.setName("Jane Smith");
        customer.setEmail("jane.smith@example.com");
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalPrice(35.50);
        
        // Verify customer fields are set
        assertNotNull(order.getCustomer());
        assertEquals("Jane Smith", order.getCustomer().getName());
        assertEquals("jane.smith@example.com", order.getCustomer().getEmail());
        assertEquals(35.50, order.getTotalPrice());
        
        // Verify guest fields are null for customer orders
        assertNull(order.getGuestName());
        assertNull(order.getGuestEmail());
    }
    
    @Test
    void testOrderCanHaveNullCustomer() {
        // Test that Order can have a null customer (for guest orders)
        Order order = new Order();
        order.setCustomer(null);
        
        // This should not throw any exception
        assertNull(order.getCustomer());
    }
}