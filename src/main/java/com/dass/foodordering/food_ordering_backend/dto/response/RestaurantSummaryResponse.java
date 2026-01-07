package com.dass.foodordering.food_ordering_backend.dto.response;

import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.SubscriptionPlan;
import lombok.Data;
import java.math.BigDecimal;

// This DTO contains ONLY the fields needed for the Super Admin list view.
@Data
public class RestaurantSummaryResponse {
    private Long id;
    private String name;
    private boolean active;
    private SubscriptionPlan plan;
    private PaymentModel paymentModel;
    private BigDecimal commissionRate;
    private boolean websiteBuilderEnabled;
    private boolean paymentsEnabled;

    public RestaurantSummaryResponse(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.active = restaurant.isActive();
        this.plan = restaurant.getPlan();
        this.paymentModel = restaurant.getPaymentModel();
        this.commissionRate = restaurant.getCommissionRate();
        this.websiteBuilderEnabled = restaurant.isWebsiteBuilderEnabled();
        this.paymentsEnabled = restaurant.isPaymentsEnabled();
    }
}