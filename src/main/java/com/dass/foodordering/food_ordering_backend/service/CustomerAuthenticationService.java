package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.config.JwtService;
import com.dass.foodordering.food_ordering_backend.auth.AuthenticationRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.CustomerRegisterRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.CustomerAuthResponse;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAuthenticationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public CustomerAuthResponse register(CustomerRegisterRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        var customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var savedCustomer = customerRepository.save(customer);
        
        var jwtToken = jwtService.generateToken(savedCustomer);
        return CustomerAuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public CustomerAuthResponse authenticate(AuthenticationRequest request) {
        var customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        
        var jwtToken = jwtService.generateToken(customer);
        return CustomerAuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}