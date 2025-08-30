package com.dass.foodordering.food_ordering_backend.dto.request;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long restaurantId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int partySize;
    private LocalDateTime reservationTime;
}