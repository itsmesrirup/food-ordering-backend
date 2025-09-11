package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.CustomerResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return new CustomerResponse(customer);
    }

    @PostMapping
    public CustomerResponse createCustomer(@RequestBody Customer customer) {
        Customer saved = customerRepository.save(customer);
        return new CustomerResponse(saved);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());

        Customer updated = customerRepository.save(customer);
        return new CustomerResponse(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerRepository.deleteById(id);
    }

    // ✅ Nested: Get all orders for a customer
    @GetMapping("/{customerId}/orders")
    public List<OrderResponse> getCustomerOrders(@PathVariable Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return customer.getOrders().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    // ✅ Nested: Create a new order for a customer
    @PostMapping("/{customerId}/orders")
    public OrderResponse createOrderForCustomer(@PathVariable Long customerId, @RequestBody Order order) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        order.setCustomer(customer);
        Order saved = orderRepository.save(order);

        return new OrderResponse(saved);
    }

    @Data
    public static class FindOrCreateRequest {
        private String email;
        private String name; // Optional name
    }

    @PostMapping("/find-or-create")
    public CustomerResponse findOrCreateCustomer(@RequestBody FindOrCreateRequest request) {
        // Try to find the customer by email first
        Customer customer = customerRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
                // If not found, create a new one
                Customer newCustomer = new Customer();
                newCustomer.setEmail(request.getEmail());
                newCustomer.setName(request.getName() != null ? request.getName() : "New Customer"); // Use name or a default
                return customerRepository.save(newCustomer);
            });
        
        return new CustomerResponse(customer);
    }
}
