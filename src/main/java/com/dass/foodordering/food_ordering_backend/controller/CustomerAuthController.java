package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.auth.AuthenticationRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.CustomerRegisterRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.CustomerAuthResponse;
import com.dass.foodordering.food_ordering_backend.service.CustomerAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<CustomerAuthResponse> register(
            @RequestBody CustomerRegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<CustomerAuthResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}