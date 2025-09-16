package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.CustomerResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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

    // --- PUBLIC ENDPOINT ---
    // For guest checkouts to create a simple customer record without a password
    @PostMapping("/find-or-create")
    public CustomerResponse findOrCreateCustomer(@RequestBody FindOrCreateRequest request) {
        // Check if customer exists by email
        Optional<Customer> existingCustomer = customerRepository.findByEmail(request.getEmail());
        
        if (existingCustomer.isPresent()) {
            return new CustomerResponse(existingCustomer.get());
        } else {
            // If not, create a new guest customer
            Customer newCustomer = new Customer();
            newCustomer.setEmail(request.getEmail());
            newCustomer.setName(request.getName() != null ? request.getName() : "Guest");
            
            // ✅ THE FIX: We must provide a non-null, but unusable, password hash.
            // We can't use a plain string because it's not a valid hash.
            // A simple placeholder for guest accounts.
            newCustomer.setPassword("$2a$10$placeholderpasswordhashforguest");
            newCustomer.setRole(Role.USER); // Also set the role
            
            return new CustomerResponse(customerRepository.save(newCustomer));
        }
    }

    // --- PROTECTED ENDPOINTS (for logged-in customers) ---

    // Get the profile of the currently logged-in customer
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile() {
        Customer currentUser = getCurrentCustomer();
        return ResponseEntity.ok(new CustomerResponse(currentUser));
    }
    
    // Get the order history of the currently logged-in customer
    @GetMapping("/me/orders")
    public List<OrderResponse> getMyOrders() {
        Customer currentUser = getCurrentCustomer();
        return currentUser.getOrders().stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }

    // You would add a PUT /me endpoint here to allow customers to update their details

    // --- HELPER METHOD ---
    private Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Customer) {
            return (Customer) principal;
        } else {
            // This case should ideally not be reached if security is configured correctly,
            // but it's good practice to handle it.
            throw new IllegalStateException("User principal is not a Customer instance");
        }
    }
}
