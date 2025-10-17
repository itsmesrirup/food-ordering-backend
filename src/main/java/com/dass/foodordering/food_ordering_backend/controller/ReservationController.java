package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.ReservationRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.ReservationResponse;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Reservation;
import com.dass.foodordering.food_ordering_backend.model.ReservationStatus;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.ReservationRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.service.EmailService;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EmailService emailService;

    // --- PUBLIC ENDPOINT ---
    // For customers to create a new reservation request.
    @PostMapping
    public ReservationResponse createReservation(@RequestBody ReservationRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        // If the feature is disabled, reject the request with a clear error message.
        if (!restaurant.isReservationsEnabled()) {
            throw new BadRequestException("This restaurant is not currently accepting online reservations.");
        }
        
        Reservation reservation = new Reservation();
        reservation.setRestaurant(restaurant);
        reservation.setCustomerName(request.getCustomerName());
        reservation.setCustomerEmail(request.getCustomerEmail());
        reservation.setCustomerPhone(request.getCustomerPhone());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    // --- PROTECTED ADMIN ENDPOINTS ---

    // For restaurant owners to get all reservations for their restaurant.
    @GetMapping("/by-restaurant")
    public List<ReservationResponse> getReservationsByRestaurant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long restaurantId = currentUser.getRestaurant().getId();

        return reservationRepository.findByRestaurantId(restaurantId).stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class UpdateStatusRequest {
        private ReservationStatus status;
    }
    
    // For restaurant owners to confirm or cancel a reservation.
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long userRestaurantId = currentUser.getRestaurant().getId();
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Security check: ensure the user can only modify reservations for their own restaurant
        if (!reservation.getRestaurant().getId().equals(userRestaurantId)) {
            // Using a generic error message to not reveal information
            throw new ResourceNotFoundException("Reservation not found");
        }
        
        reservation.setStatus(request.getStatus());
        Reservation updatedReservation = reservationRepository.save(reservation);
        if (updatedReservation.getStatus() == ReservationStatus.CONFIRMED) {
            emailService.sendReservationConfirmedNotification(updatedReservation);
        }
        return ResponseEntity.ok(new ReservationResponse(updatedReservation));
    }
}