package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.ContactFormRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.RestaurantResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.service.EmailService;
import com.dass.foodordering.food_ordering_backend.service.FeatureService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public") // A dedicated path for public endpoints
public class PublicController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EmailService emailService;

    @Autowired FeatureService featureService;

    @GetMapping("/restaurants/by-slug/{slug}")
    public ResponseEntity<RestaurantResponse> getRestaurantBySlug(@PathVariable String slug) {
        Restaurant restaurant = restaurantRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
       
        // --- ADDED: Check if WEBSITE_BUILDER is enabled ---
        if (!featureService.isFeatureAvailable(restaurant, "WEBSITE_BUILDER")) {
            // Option A: Return 404 (act like it doesn't exist)
            throw new ResourceNotFoundException("Website not enabled for this restaurant.");
            
            // Option B: Return 403 Forbidden
            // return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(new RestaurantResponse(restaurant));
    }

    @PostMapping("/contact")
    public ResponseEntity<Void> submitContactForm(@RequestBody ContactFormRequest request) {
        emailService.sendContactFormNotification(request);
        return ResponseEntity.ok().build();
    }
}