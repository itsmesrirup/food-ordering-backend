package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.response.CommissionLedgerResponse;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CommissionLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    @Autowired
    private CommissionLedgerRepository commissionLedgerRepository;

    @GetMapping("/my-restaurant")
    public List<CommissionLedgerResponse> getMyRestaurantCommissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long restaurantId = currentUser.getRestaurant().getId();

        return commissionLedgerRepository.findByRestaurantIdOrderByTransactionDateDesc(restaurantId)
                .stream()
                .map(CommissionLedgerResponse::new)
                .collect(Collectors.toList());
    }

    // --- ADDED: New endpoint for the Super Admin ---
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // Secure the endpoint
    public List<CommissionLedgerResponse> getAllCommissions() {
        // Fetch ALL ledger entries and sort by date
        return commissionLedgerRepository.findAllByOrderByTransactionDateDesc()
                .stream()
                .map(CommissionLedgerResponse::new)
                .collect(Collectors.toList());
    }
}