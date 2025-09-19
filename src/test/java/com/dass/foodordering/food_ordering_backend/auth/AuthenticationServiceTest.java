package com.dass.foodordering.food_ordering_backend.auth;

import com.dass.foodordering.food_ordering_backend.config.JwtService;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Restaurant testRestaurant;
    private RegisterRequest validRegisterRequest;
    private RegisterRequest invalidRegisterRequest;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");

        validRegisterRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .restaurantId(1L)
                .build();

        invalidRegisterRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .restaurantId(null)  // This should trigger BadRequestException
                .build();
    }

    @Test
    void register_ShouldThrowBadRequestException_WhenRestaurantIdIsNull() {
        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authenticationService.register(invalidRegisterRequest)
        );

        assertEquals("Please sign up from a restaurant's page.", exception.getMessage());
        
        // Verify that no repository calls were made
        verify(restaurantRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowResourceNotFoundException_WhenRestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authenticationService.register(validRegisterRequest)
        );

        assertEquals("Cannot register user: Restaurant not found", exception.getMessage());
    }

    @Test
    void register_ShouldSucceed_WhenRestaurantIdIsValidAndRestaurantExists() {
        // Arrange
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        
        User savedUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .restaurant(testRestaurant)
                .role(Role.ADMIN)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        AuthenticationResponse response = authenticationService.register(validRegisterRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        
        verify(restaurantRepository).findById(1L);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }
}