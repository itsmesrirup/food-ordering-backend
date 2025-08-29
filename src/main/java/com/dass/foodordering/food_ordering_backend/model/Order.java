package com.dass.foodordering.food_ordering_backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "orders") // "order" is reserved in SQL
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // keep it simple as String for now (PENDING/CONFIRMED/CANCELLED, etc.)
    //private String status;

    @Column(nullable = false)
    private double totalPrice = 0.0;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // ✅ Add restaurant relation so setRestaurant(...) exists
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // Owning side is MenuItem (it has @ManyToOne Order)
    //@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    //private List<MenuItem> menuItems = new ArrayList<>();

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    // CHANGE THIS:
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    // --- helper methods ---
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // --- getters/setters ---
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> items) { this.orderItems = items; }

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // --- helpers to keep both sides in sync ---
    //public void addMenuItem(MenuItem item) {
      //  menuItems.add(item);
        //item.setOrder(this);
        //totalPrice += item.getPrice(); // simple running total
    //}

    //public void removeMenuItem(MenuItem item) {
      //  if (menuItems.remove(item)) {
        //    item.setOrder(null);
          //  totalPrice -= item.getPrice();
        //}
    //}

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    //public List<MenuItem> getMenuItems() { return menuItems; }
    //public void setMenuItems(List<MenuItem> items) {
      //  this.menuItems.clear();
        //if (items != null) {
          //  for (MenuItem mi : items) {
            //    addMenuItem(mi);
            //}
        //}
    //}
}
