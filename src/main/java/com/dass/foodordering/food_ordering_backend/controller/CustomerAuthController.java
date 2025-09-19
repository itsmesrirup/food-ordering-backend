package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.CustomerRegisterRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.CustomerAuthResponse;
import com.dass.foodordering.food_ordering_backend.auth.AuthenticationRequest;
import com.dass.foodordering.food_ordering_backend.service.CustomerAuthenticationService;
import com.dass.foodordering.food_ordering_backend.service.CustomerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/customer")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthenticationService authenticationService;
    private final CustomerRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<CustomerAuthResponse> register(@RequestBody CustomerRegisterRequest request) {
        try {
            // Link the customer to the restaurant
            registrationService.linkCustomerToRestaurant(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRestaurantId()
            );
            // Now handle authentication and JWT
            CustomerAuthResponse response = authenticationService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    CustomerAuthResponse.builder()
                            .customerId(null)
                            .email(request.getEmail())
                            .message(e.getMessage())
                            .token(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    CustomerAuthResponse.builder()
                            .customerId(null)
                            .email(request.getEmail())
                            .message("Internal error: " + e.getMessage())
                            .token(null)
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerAuthResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            CustomerAuthResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                CustomerAuthResponse.builder()
                        .customerId(null)
                        .email(request.getEmail())
                        .message(e.getMessage())
                        .token(null)
                        .build()
            );
        }
    }
}