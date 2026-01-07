package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.oauth.TokenResponse;
import com.stripe.net.OAuth;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired private RestaurantRepository restaurantRepository;

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.client.id}")
    private String clientId;
    
    @Value("${stripe.redirect.uri}")
    private String redirectUri;

    // 1. Generate the Link for the Restaurant Admin to click
    @GetMapping("/onboarding-link")
    public ResponseEntity<Map<String, String>> getOnboardingLink() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // We use state to pass the restaurant ID securely through the redirect flow
        String state = currentUser.getRestaurant().getId().toString();

        String url = "https://connect.stripe.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&scope=read_write" +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    // 2. Handle the callback after they sign up/login to Stripe
    @PostMapping("/authorize-merchant")
    public ResponseEntity<Void> authorizeMerchant(@RequestBody Map<String, String> payload) throws StripeException {
        Stripe.apiKey = apiKey;
        
        String authCode = payload.get("code");
        
        // --- CHANGED: Use a Map instead of TokenCreateParams builder ---
        // This avoids the import error completely.
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", authCode);

        // Exchange the code for the Merchant's Stripe Account ID
        // OAuth.token() accepts a Map in all versions
        TokenResponse response = OAuth.token(params, RequestOptions.builder().build());
        
        String connectedAccountId = response.getStripeUserId();

        // ... (rest of the save logic is unchanged) ...
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Restaurant restaurant = currentUser.getRestaurant();
        restaurant.setStripeAccountId(connectedAccountId);
        restaurant.setStripeDetailsSubmitted(true);
        restaurantRepository.save(restaurant);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> payload) throws StripeException {
        
        // We must initialize the Stripe library with your Secret Key before doing anything else
        Stripe.apiKey = apiKey;

        // 1. Calculate Amount (Frontend sends amount or cart items, safer to recalc on backend but for now trust frontend amount for simplicity or recalc)
        // Let's assume frontend sends { "amount": 2550, "currency": "eur", "restaurantId": 1 }
        Long amount = Long.parseLong(payload.get("amount").toString());
        String currency = (String) payload.get("currency");
        Long restaurantId = Long.parseLong(payload.get("restaurantId").toString());

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
        
        // 2. Calculate Application Fee (My Commission)
        // e.g. 5% = 0.05
        BigDecimal commissionRate = restaurant.getCommissionRate() != null ? restaurant.getCommissionRate() : BigDecimal.ZERO; 
        long applicationFee = BigDecimal.valueOf(amount).multiply(commissionRate).longValue();

        // 3. Create Intent with Connect parameters
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount)
            .setCurrency(currency)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .setApplicationFeeAmount(applicationFee) // YOUR CUT
            .setTransferData(
                PaymentIntentCreateParams.TransferData.builder()
                    .setDestination(restaurant.getStripeAccountId()) // RESTAURANT'S STRIPE ID
                    .build()
            )
            .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        return ResponseEntity.ok(response);
    }
}