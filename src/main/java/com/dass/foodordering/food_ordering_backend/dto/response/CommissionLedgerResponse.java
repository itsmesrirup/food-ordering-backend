package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.CommissionLedger;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionLedgerResponse {

    private Long id;
    private Long orderId;
    private LocalDateTime transactionDate;
    private BigDecimal orderTotal;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private String restaurantName;

    public CommissionLedgerResponse(CommissionLedger ledger) {
        this.id = ledger.getId();
        this.orderId = ledger.getOrder().getId(); // Get the ID from the related Order object
        this.transactionDate = ledger.getTransactionDate();
        this.orderTotal = ledger.getOrderTotal();
        this.commissionRate = ledger.getCommissionRate();
        this.commissionAmount = ledger.getCommissionAmount();
        this.restaurantName = ledger.getRestaurant().getName();
    }
}