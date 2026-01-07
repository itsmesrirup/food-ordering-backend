package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.UpdatePasswordRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.UserResponse;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.UserRepository;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- DTO for creating a new staff member ---
    @Data
    public static class CreateStaffRequest {
        private String email;
        private String password;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        // Spring Security stores the authenticated user's details in the SecurityContext.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(new UserResponse(currentUser));
    }

    // --- PROTECTED ADMIN ENDPOINTS ---

    // For a restaurant owner to get a list of their staff accounts
    @GetMapping("/my-staff")
    public List<UserResponse> getMyStaff() {
        User currentUser = getCurrentUser();
        // Find all users belonging to the same restaurant
        return userRepository.findByRestaurantId(currentUser.getRestaurant().getId())
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
    
    // For a restaurant owner to create a new KITCHEN_STAFF user
    @PostMapping("/my-staff")
    public UserResponse createKitchenStaff(@RequestBody CreateStaffRequest request) {
        User currentUser = getCurrentUser();
        Restaurant restaurant = currentUser.getRestaurant();

        // Check if email is already in use for this restaurant
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use.");
        }

        User newStaff = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .restaurant(restaurant)
                .role(Role.KITCHEN_STAFF) // Assign the KITCHEN_STAFF role
                .build();
        
        return new UserResponse(userRepository.save(newStaff));
    }
    
    @DeleteMapping("/my-staff/{id}")
    public ResponseEntity<Void> deleteStaffMember(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        User staffMember = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security check: ensure the admin can only delete staff from their own restaurant
        if (!staffMember.getRestaurant().getId().equals(currentUser.getRestaurant().getId())) {
             throw new ResourceNotFoundException("User not found");
        }
        // Prevent an admin from deleting themselves
        if (staffMember.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot delete your own account.");
        }

        userRepository.delete(staffMember);
        return ResponseEntity.noContent().build();
    }

    // --- ADDED: Endpoint for Super Admin to reset ANY restaurant admin's password ---
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/restaurant/{restaurantId}/admin/reset-password")
    public ResponseEntity<Void> resetRestaurantAdminPassword(
            @PathVariable Long restaurantId, 
            @RequestBody UpdatePasswordRequest request) {

        // 1. Find all users for this restaurant
        List<User> restaurantUsers = userRepository.findByRestaurantId(restaurantId);
        
        // 2. Find the ADMIN user (assuming one admin per restaurant for now)
        User adminUser = restaurantUsers.stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No Admin user found for this restaurant"));

        // 3. Update Password
        adminUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(adminUser);
        
        return ResponseEntity.noContent().build();
    }

    // --- Helper Method ---
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}