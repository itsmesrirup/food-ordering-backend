package com.dass.foodordering.food_ordering_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dass.foodordering.food_ordering_backend.model.Reservation;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRestaurantId(Long restaurantId);
}
