package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeatureService {

    // This map is the single source of truth for your subscription plans.
    private static final Map<SubscriptionPlan, Set<String>> planFeatures = Map.of(
        SubscriptionPlan.BASIC, Set.of(
            "ORDERS", "MENU"
        ),
        SubscriptionPlan.PRO, Set.of(
            "ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING"
        ),
        SubscriptionPlan.PREMIUM, Set.of(
            "ORDERS", "MENU", "RESERVATIONS", "QR_ORDERING", "ANALYTICS", "RECOMMENDATIONS"
        )
    );

    public Set<String> getFeaturesForPlan(SubscriptionPlan plan) {
        return planFeatures.getOrDefault(plan, Set.of());
    }

    public boolean isFeatureAvailable(SubscriptionPlan plan, String feature) {
        return getFeaturesForPlan(plan).contains(feature);
    }


    public Set<String> getAvailableFeatures(SubscriptionPlan plan) {
        return planFeatures.entrySet().stream()
                .filter(entry -> entry.getKey().ordinal() <= plan.ordinal())
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }
}