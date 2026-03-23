package com.dass.foodordering.food_ordering_backend.config;

import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Component
public class DynamicCorsConfigurationSource implements CorsConfigurationSource {

    private final RestaurantRepository restaurantRepository;

    // These are your CORE platform domains. These never change.
    private final List<String> CORE_PLATFORM_DOMAINS = Arrays.asList(
        "https://food-ordering-admin-mvp.netlify.app",
        "https://food-ordering-customer-mvp.netlify.app",
        "http://localhost:5173", 
        "http://localhost:5174"
    );

    public DynamicCorsConfigurationSource(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        
        if (origin == null) return null;

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        // 1. Is it coming from your main admin/customer platform?
        if (CORE_PLATFORM_DOMAINS.contains(origin)) {
            config.setAllowedOrigins(Arrays.asList(origin));
            return config;
        }

        // 2. Is it coming from a Custom Restaurant Domain?
        // Strip https:// or http:// to match what you save in the DB (e.g., "www.tikkanway.fr")
        String cleanDomain = origin.replace("https://", "").replace("http://", "");
        
        // Query the database dynamically!
        if (restaurantRepository.existsByCustomDomain(cleanDomain)) {
            config.setAllowedOrigins(Arrays.asList(origin)); // Allow this specific restaurant's domain
            return config;
        }

        // 3. If it's an unknown website trying to steal your API data, reject it.
        return null; 
    }
}