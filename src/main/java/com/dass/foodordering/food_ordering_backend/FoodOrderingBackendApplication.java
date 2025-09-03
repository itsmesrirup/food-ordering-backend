package com.dass.foodordering.food_ordering_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

	/**
     * ✅ TEMPORARY UTILITY: This bean will run once on startup.
     * It will hash a password and print it to the console.
     * After you get the hash, you should comment out or delete this bean.
     */
    /*@Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            // Create a new instance of the password encoder
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            // Choose your secure super admin password
            String plainPassword = "Welcome123!";
            
            // Hash the password
            String hashedPassword = encoder.encode(plainPassword);
            
            // Print it to the console so you can copy it
            System.out.println("--- PASSWORD HASH GENERATOR ---");
            System.out.println("Plain Password: " + plainPassword);
            System.out.println("Hashed Password for SQL script: " + hashedPassword);
            System.out.println("---------------------------------");
        };
    }*/
	
	@PostConstruct
    public void testDb() {
        System.out.println("Restaurants in DB: " + restaurantRepository.findAll().size());
    }

}
