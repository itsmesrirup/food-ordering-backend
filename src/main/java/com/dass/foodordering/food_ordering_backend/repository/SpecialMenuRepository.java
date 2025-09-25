package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.SpecialMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialMenuRepository extends JpaRepository<SpecialMenu, Long> {

    List<SpecialMenu> findByRestaurantId(Long restaurantId);

    // This custom query finds the first active special menu where today's date
    // is between the start and end dates.
    Optional<SpecialMenu> findFirstByRestaurantIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long restaurantId, LocalDate today1, LocalDate today2);

}