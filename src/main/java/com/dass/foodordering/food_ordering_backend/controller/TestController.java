package com.dass.foodordering.food_ordering_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/api/public/error-test")
    public void testError() {
        throw new RuntimeException("This is a test error for Sentry!");
    }

}
