package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.CommissionLedger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {
    // --- ADDED: This method is required by the CommissionController ---
    // Spring Data JPA will generate the query:
    // "SELECT * FROM commission_ledger WHERE restaurant_id = ? ORDER BY transaction_date DESC"
    List<CommissionLedger> findByRestaurantIdOrderByTransactionDateDesc(Long restaurantId);
    List<CommissionLedger>  findAllByOrderByTransactionDateDesc();
}