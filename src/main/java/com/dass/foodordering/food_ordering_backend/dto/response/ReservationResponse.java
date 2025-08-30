package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.Reservation;
import com.dass.foodordering.food_ordering_backend.model.ReservationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long restaurantId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int partySize;
    private LocalDateTime reservationTime;
    private ReservationStatus status;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.restaurantId = reservation.getRestaurant().getId();
        this.customerName = reservation.getCustomerName();
        this.customerEmail = reservation.getCustomerEmail();
        this.customerPhone = reservation.getCustomerPhone();
        this.partySize = reservation.getPartySize();
        this.reservationTime = reservation.getReservationTime();
        this.status = reservation.getStatus();
    }
}