package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.MenuItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
}
