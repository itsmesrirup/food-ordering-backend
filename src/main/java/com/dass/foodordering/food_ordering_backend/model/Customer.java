package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data // This Lombok annotation generates getters, setters, equals, hashCode, and toString
@Builder
@AllArgsConstructor
public class Customer implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // --- NEW FIELDS ---
    
    @Column(nullable = false)
    private String password; // Will be hashed

    private String phone;
    private LocalDate birthday;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist // This JPA annotation sets the creation date automatically before saving
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // All customers have the USER role

    // --- UserDetails Implementation (for Spring Security) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() { 
        return email; // Use email as the username
    }
    
    @Override
    public String getPassword() {
        return password; // The required method
    }

    @Override
    public boolean isAccountNonExpired() { 
        return true; // For now, accounts don't expire
    }

    @Override
    public boolean isAccountNonLocked() { 
        return true; // For now, accounts don't lock
    }

    @Override
    public boolean isCredentialsNonExpired() { 
        return true; // For now, passwords don't expire
    }

    @Override
    public boolean isEnabled() { 
        return true; // All accounts are enabled by default
    }

    // Constructors
    public Customer() {}
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}
