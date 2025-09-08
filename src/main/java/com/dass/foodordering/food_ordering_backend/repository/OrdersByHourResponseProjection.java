package com.dass.foodordering.food_ordering_backend.repository;

// This is a public interface, so it lives in its own file.
public interface OrdersByHourResponseProjection {
    Integer getHour();
    Long getOrderCount();
}