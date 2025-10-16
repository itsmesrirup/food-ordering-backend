package com.dass.foodordering.food_ordering_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- CRITICAL IMPORT
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // --- PUBLIC Endpoints ---
                .requestMatchers("/actuator/health", "/api/auth/**", "/api/customer/auth/**", "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/special-menus/restaurant/**", "/api/analytics/recommendations/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers/find-or-create", "/api/orders", "/api/reservations").permitAll()

                // --- USER (Customer) Endpoints ---
                .requestMatchers("/api/customers/me/**").hasRole("USER")

                // --- KITCHEN_STAFF Endpoints ---
                .requestMatchers("/api/orders/by-restaurant/kitchen").hasRole("KITCHEN_STAFF")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/{id}/status").hasRole("KITCHEN_STAFF")
                
                // --- ADMIN Endpoints ---
                // Because of Role Hierarchy, ADMIN can also access KITCHEN_STAFF endpoints.
                .requestMatchers("/api/analytics/**", "/api/categories/**", "/api/users/my-staff/**").hasRole("ADMIN")
                .requestMatchers("/api/menu-items/**", "/api/menu-item-options/**", "/api/menu-item-option-choices/**").hasRole("ADMIN")
                .requestMatchers("/api/special-menus/**").hasRole("ADMIN") // General rule for all special menu management
                .requestMatchers(HttpMethod.PUT, "/api/restaurants/{id}").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasRole("ADMIN") // General rule for any other order management
                .requestMatchers("/api/commissions/my-restaurant").hasRole("ADMIN")

                // --- SUPER_ADMIN Endpoints ---
                // SUPER_ADMIN can access everything above, plus these.
                .requestMatchers("/api/restaurants/all").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/restaurants/{id}").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/restaurants/{id}/reactivate").hasRole("SUPER_ADMIN")
                
                // This rule is for both ADMIN and SUPER_ADMIN because KITCHEN_STAFF doesn't have a /me endpoint
                .requestMatchers("/api/users/me").hasAnyRole("ADMIN", "SUPER_ADMIN","KITCHEN_STAFF")

                // --- Default Deny ---
                .anyRequest().authenticated()
            )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_SUPER_ADMIN > ROLE_ADMIN\nROLE_ADMIN > ROLE_KITCHEN_STAFF\nROLE_KITCHEN_STAFF > ROLE_USER");
        return hierarchy;
    }

    @Bean
    public static DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
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