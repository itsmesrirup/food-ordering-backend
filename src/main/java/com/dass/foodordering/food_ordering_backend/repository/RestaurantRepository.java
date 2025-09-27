package com.dass.foodordering.food_ordering_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // This is for PUBLIC use (customer app). It ONLY finds active restaurants.
    List<Restaurant> findByActiveTrue();
    Optional<Restaurant> findByIdAndActiveTrue(Long id);
    
    // This is for SUPER ADMIN use. It finds ALL restaurants.
    @Query("SELECT r FROM Restaurant r WHERE r.id = :id")
    Optional<Restaurant> findEvenInactiveById(@Param("id") Long id);
    @Query("SELECT r FROM Restaurant r") // This custom query bypasses the @Where clause
    List<Restaurant> findAllEvenInactive(); 
}
