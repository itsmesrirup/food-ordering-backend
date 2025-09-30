package com.dass.foodordering.food_ordering_backend.model;

public enum Role {
    USER,        // For regular customers, if you add customer logins later
    ADMIN,       // For Restaurant Owners
    KITCHEN_STAFF, // ✅ NEW: For cooks and kitchen staff
    SUPER_ADMIN  // For you
}