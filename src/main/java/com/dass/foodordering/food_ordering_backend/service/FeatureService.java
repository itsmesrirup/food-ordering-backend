package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeatureService {

    // This map is the single source of truth for your subscription plans.
    private static final Map<SubscriptionPlan, Set<String>> planFeatures = Map.of(
        SubscriptionPlan.BASIC, Set.of("ORDERS", "MENU"),
        SubscriptionPlan.PRO, Set.of("ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING"),
        SubscriptionPlan.PREMIUM, Set.of("ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING", "ANALYTICS", "RECOMMENDATIONS", "WEBSITE_BUILDER")
    );

    // --- Define the feature set for the Commission model ---
    private static final Set<String> commissionFeatures = Set.of(
        "ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING" // Equivalent to PRO
    );

    public Set<String> getAvailableFeaturesForRestaurant(Restaurant restaurant) {
        if (restaurant.getPaymentModel() == PaymentModel.COMMISSION) {
            return commissionFeatures;
        }
        
        // If it's a subscription model, use the plan-based logic
        return planFeatures.getOrDefault(restaurant.getPlan(), Set.of());
    }

    public Set<String> getFeaturesForPlan(SubscriptionPlan plan) {
        return planFeatures.getOrDefault(plan, Set.of());
    }

    public boolean isFeatureAvailable(Restaurant restaurant, String feature) {
        return getAvailableFeaturesForRestaurant(restaurant).contains(feature);
    }


    public Set<String> getAvailableFeatures(SubscriptionPlan plan) {
        return planFeatures.entrySet().stream()
                .filter(entry -> entry.getKey().ordinal() <= plan.ordinal())
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }
}