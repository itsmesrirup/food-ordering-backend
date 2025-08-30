package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int partySize;
    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // PENDING, CONFIRMED, CANCELLED
}