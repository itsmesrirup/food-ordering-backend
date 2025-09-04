package com.dass.foodordering.food_ordering_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- CRITICAL IMPORT
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Make sure these are here
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // ✅ THIS IS THE CRITICAL CHANGE. We are now explicitly telling
                // Spring Security to use the configuration from our CorsConfigurationSource bean.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                    // Public endpoints
                .requestMatchers("/actuator/health", "/api/auth/**").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers/find-or-create", "/api/orders", "/api/reservations").permitAll()
                // Allow both ADMIN and SUPER_ADMIN to get their own profile
                .requestMatchers("/api/users/me").hasAnyAuthority("ADMIN", "SUPER_ADMIN")

                // ADMIN endpoints (for restaurant owners)
                .requestMatchers(HttpMethod.PUT, "/api/restaurants/{id}").hasAuthority("ADMIN")
                .requestMatchers("/api/reservations/by-restaurant", "/api/reservations/{id}/status").hasAuthority("ADMIN")
                .requestMatchers("/api/menu-items/**").hasAuthority("ADMIN")
                .requestMatchers("/api/orders/{id}/**").hasAuthority("ADMIN") // For status updates, etc.
                .requestMatchers("/api/users/me").hasAuthority("ADMIN") // Getting their own profile
                .requestMatchers("/api/categories/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/menu-items/{id}/availability").hasAuthority("ADMIN")
                
                // SUPER_ADMIN endpoints (for you)
                .requestMatchers(HttpMethod.POST, "/api/restaurants").hasAuthority("SUPER_ADMIN")
                // We would add more super admin routes here, e.g., GET /api/users, GET /api/restaurants/all-admin-list
                
                // Default deny
                .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    // Make sure your CorsFilter bean is still here and configured correctly
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Add BOTH of your frontend ports here just in case.
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173", //local admin
                "https://food-ordering-admin-mvp.netlify.app", // live admin app
                "http://localhost:5174", // local customer ordering app
                "https://food-ordering-customer-mvp.netlify.app/", // live customer ordering app
                "http://localhost:5175"));
        config.addAllowedHeader("*"); // Allow all headers
        config.addAllowedMethod("*");  // Allow all methods (GET, POST, etc.)
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}