package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.model.*;
import com.dass.foodordering.food_ordering_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final CustomerRestaurantRepository customerRestaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Customer linkCustomerToRestaurant(String name, String email, String password, Long restaurantId) {
        Customer customer = customerRepository.findByEmail(email)
            .orElseGet(() -> {
                Customer c = Customer.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(password)) // Hash the password
                        .role(Role.USER)
                        .build();
                return customerRepository.save(c);
            });

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        if (!customerRestaurantRepository.existsByCustomerIdAndRestaurantId(customer.getId(), restaurantId)) {
            CustomerRestaurant cr = CustomerRestaurant.builder()
                    .id(new CustomerRestaurantId(customer.getId(), restaurantId))
                    .customer(customer)
                    .restaurant(restaurant)
                    .registeredAt(LocalDateTime.now())
                    .loyaltyPoints(0)
                    .build();
            customerRestaurantRepository.save(cr);
        }

        return customer;
    }
}