package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class CommissionLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private BigDecimal orderTotal;

    @Column(nullable = false)
    private BigDecimal commissionRate;
    
    @Column(nullable = false)
    private BigDecimal commissionAmount;

    @Column(nullable = false)
    private LocalDateTime transactionDate;
}