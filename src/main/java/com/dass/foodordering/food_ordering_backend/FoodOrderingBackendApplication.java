package com.dass.foodordering.food_ordering_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class FoodOrderingBackendApplication {

	private final RestaurantRepository restaurantRepository;

	public FoodOrderingBackendApplication(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

	public static void main(String[] args) {
		SpringApplication.run(FoodOrderingBackendApplication.class, args);
	}

	@PostConstruct
    public void testDb() {
        System.out.println("Restaurants in DB: " + restaurantRepository.findAll().size());
    }

}
