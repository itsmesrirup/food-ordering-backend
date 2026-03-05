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
                .requestMatchers("/actuator/health", "/api/auth/**", "/api/customer/auth/**", "/api/public/**", "/ws/**").permitAll()
                // IMPORTANT: POST /api/orders MUST be public for customers.
                .requestMatchers(HttpMethod.POST, "/api/customers/find-or-create", "/api/orders", "/api/reservations").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/special-menus/restaurant/**", "/api/analytics/recommendations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/{id}").permitAll()
                .requestMatchers("/api/payments/create-intent").permitAll()
                .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/restaurants/by-slug-full/**").permitAll()

                // --- USER (Customer) Endpoints ---
                .requestMatchers("/api/customers/me/**").hasRole("USER")

                // --- STAFF (Waiter + Admin) Endpoints ---
                // Waiters & Admins need to fetch the menu data for the POS
                // Note: We use hasAnyRole to be explicit, even with hierarchy
                .requestMatchers(HttpMethod.GET, "/api/menu-items/by-restaurant").hasAnyRole("WAITER", "ADMIN") 
                .requestMatchers(HttpMethod.GET, "/api/categories/by-restaurant").hasAnyRole("WAITER", "ADMIN")
                
                // Waiters & Admins need to see order status
                .requestMatchers(HttpMethod.GET, "/api/orders/by-restaurant").hasAnyRole("WAITER", "ADMIN")

                // --- KITCHEN Endpoints ---
                // Waiters should arguably NOT see the full Kitchen KDS, just their orders
                .requestMatchers("/api/orders/by-restaurant/kitchen").hasAnyRole("KITCHEN_STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/{id}/status").hasAnyRole("KITCHEN_STAFF", "ADMIN")
                
                // --- ADMIN ONLY Endpoints ---
                // These effectively block Waiters from editing/deleting
                .requestMatchers("/api/analytics/**", "/api/users/my-staff/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/menu-items/**").hasRole("ADMIN") // Only Admin can CREATE menu items
                .requestMatchers(HttpMethod.PUT, "/api/menu-items/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/menu-items/**").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").hasRole("ADMIN") // Admin manages categories
                .requestMatchers("/api/special-menus/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/restaurants/{id}").hasRole("ADMIN")
                .requestMatchers("/api/commissions/my-restaurant").hasRole("ADMIN")

                // --- SUPER_ADMIN Endpoints ---
                .requestMatchers("/api/restaurants/all").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/restaurants/{id}").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/restaurants/{id}/reactivate").hasRole("SUPER_ADMIN")
                
                // --- SHARED Authenticated ---
                // Waiters need /me to load their profile
                .requestMatchers("/api/users/me").authenticated() 

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
        hierarchy.setHierarchy(
            "ROLE_SUPER_ADMIN > ROLE_ADMIN\n" +
            "ROLE_ADMIN > ROLE_KITCHEN_STAFF\n" +
            "ROLE_ADMIN > ROLE_WAITER\n" + // Admin manages Waiters
            "ROLE_KITCHEN_STAFF > ROLE_USER\n" +
            "ROLE_WAITER > ROLE_USER"
        );
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