package com.dass.foodordering.food_ordering_backend.repository;

import com.dass.foodordering.food_ordering_backend.model.Category;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Spring Data JPA automatically creates the query by method name
    List<Category> findByRestaurantAndParentCategoryIsNull(Restaurant restaurant);

    // Find a top-level category by name for a specific restaurant
    Optional<Category> findByNameAndRestaurantAndParentCategoryIsNull(String name, Restaurant restaurant);

    // Find a subcategory by name and its parent
    Optional<Category> findByNameAndParentCategory(String name, Category parentCategory);
}