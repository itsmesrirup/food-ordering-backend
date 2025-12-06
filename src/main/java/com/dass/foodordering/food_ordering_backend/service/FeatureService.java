package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeatureService {

    // This map is the single source of truth for your subscription plans.
    private static final Map<SubscriptionPlan, Set<String>> planFeatures = Map.of(
        SubscriptionPlan.BASIC, Set.of("ORDERS", "MENU"),
        SubscriptionPlan.PRO, Set.of("ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING"),
        SubscriptionPlan.PREMIUM, Set.of("ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING", "ANALYTICS", "RECOMMENDATIONS")
    );

    // --- Define the feature set for the Commission model ---
    private static final Set<String> commissionFeatures = Set.of(
        "ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING" // Equivalent to PRO
    );

    public Set<String> getAvailableFeaturesForRestaurant(Restaurant restaurant) {
        Set<String> features = new HashSet<>();

        // 1. Base Features
        if (restaurant.getPaymentModel() == PaymentModel.COMMISSION) {
            features.addAll(commissionFeatures);
        } else {
            features.addAll(planFeatures.getOrDefault(restaurant.getPlan(), Set.of()));
        }

        // 2. The "Add-On" Logic (Renamed)
        // If this flag is true, they get the website builder, regardless of plan/commission.
        if (restaurant.isWebsiteBuilderEnabled()) {
            features.add("WEBSITE_BUILDER");
        }

        return features;
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