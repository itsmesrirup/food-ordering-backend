package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.OrderItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.OrderRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.CommissionLedger;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.OrderItem;
import com.dass.foodordering.food_ordering_backend.model.OrderStatus;
import com.dass.foodordering.food_ordering_backend.model.PaymentModel;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CommissionLedgerRepository;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CommissionLedgerRepository commissionLedgerRepository;

    @Autowired
    private EmailService emailService;

    @Data
    public static class UpdateStatusRequest {
        private OrderStatus status;
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderResponse(order);
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) throws JsonProcessingException {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: "+request.getCustomerId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item.");
        }
        
        // Fetch all menu items in one go for efficiency
        List<Long> menuItemIds = request.getItems().stream().map(OrderItemRequest::getMenuItemId).collect(Collectors.toList());
        List<MenuItem> menuItems = menuItemRepository.findAllById(menuItemIds);
        
        if (menuItems.size() != menuItemIds.size()) {
            throw new ResourceNotFoundException("One or more menu items not found.");
        }

        // Check if all items belong to the same restaurant
        Restaurant restaurant = menuItems.get(0).getRestaurant();
        boolean allFromSameRestaurant = menuItems.stream()
                .allMatch(item -> item.getRestaurant().getId().equals(restaurant.getId()));

        if (!allFromSameRestaurant) {
            throw new BadRequestException("All menu items must belong to the same restaurant.");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());

        if (request.getTableNumber() != null && !request.getTableNumber().isEmpty()) {
            if (!restaurant.isQrCodeOrderingEnabled()) {
                // The feature is disabled for this restaurant, so reject the order.
                throw new BadRequestException("QR Code ordering is not enabled for this restaurant.");
            }
            order.setTableNumber(request.getTableNumber());
        }
        
        // Create OrderItem objects and calculate total price
        double totalPrice = 0;
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItems.stream()
                .filter(mi -> mi.getId().equals(itemRequest.getMenuItemId()))
                .findFirst().orElseThrow();

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());

            // Save the structured selected options
            if (itemRequest.getSelectedOptions() != null && !itemRequest.getSelectedOptions().isEmpty()) {
                orderItem.setSelectedOptionsFromList(itemRequest.getSelectedOptions());
            }

            order.addOrderItem(orderItem);

            totalPrice += menuItem.getPrice() * itemRequest.getQuantity();
        }
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        // --- ADDED: Commission Calculation Logic ---
        Restaurant savedRestaurant = savedOrder.getRestaurant();
        
        // Check if the restaurant is on a commission-based plan
        if (savedRestaurant.getPaymentModel() == PaymentModel.COMMISSION && savedRestaurant.getCommissionRate() != null) {
            BigDecimal orderTotal = BigDecimal.valueOf(savedOrder.getTotalPrice());
            BigDecimal commissionRate = savedRestaurant.getCommissionRate();
            
            // Calculate commission: total * rate. Scale to 2 decimal places.
            BigDecimal commissionAmount = orderTotal.multiply(commissionRate)
                                                    .setScale(2, RoundingMode.HALF_UP);

            // Create and save a ledger entry for this transaction
            CommissionLedger ledgerEntry = new CommissionLedger();
            ledgerEntry.setRestaurant(savedRestaurant);
            ledgerEntry.setOrder(savedOrder);
            ledgerEntry.setOrderTotal(orderTotal);
            ledgerEntry.setCommissionRate(commissionRate);
            ledgerEntry.setCommissionAmount(commissionAmount);
            ledgerEntry.setTransactionDate(LocalDateTime.now());
            
            commissionLedgerRepository.save(ledgerEntry);
        }

        emailService.sendNewOrderNotification(savedOrder);
        return new OrderResponse(savedOrder);
    }

    /*@PutMapping("/{id}")
    public OrderResponse updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(orderDetails.getStatus());
        order.setTotalPrice(orderDetails.getTotalPrice());

        Order updated = orderRepository.save(order);
        return new OrderResponse(updated);
    }*/

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        // Find the order first
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Get the logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Long userRestaurantId = currentUser.getRestaurant().getId();

        // SECURITY CHECK: Ensure the order belongs to the current user's restaurant
        if (!order.getRestaurant().getId().equals(userRestaurantId)) {
            // Throw an exception. We use ResourceNotFound to avoid revealing that the order exists.
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }

        orderRepository.delete(order);
        
        // Return a 204 No Content response, which is the standard for a successful DELETE
        return ResponseEntity.noContent().build();
    }

    // ✅ Nested: Add MenuItem to an Order
    /*@PostMapping("/{orderId}/items")
    public MenuItemResponse addItemToOrder(
            @PathVariable Long orderId,
            @RequestBody MenuItemRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setDescription(request.getDescription());
        menuItem.setOrder(order);
        menuItem.setRestaurant(restaurant); // ✅ ensure restaurant is always set

        MenuItem saved = menuItemRepository.save(menuItem);
        return new MenuItemResponse(saved);
    }*/

    // ✅ Nested: Get all items of an Order
    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> getOrderItems(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Call the new getOrderItems() and map to the new OrderItemResponse DTO
        return order.getOrderItems().stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
    }
    
    @PatchMapping("/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setStatus(request.getStatus());
        
        Order updatedOrder = orderRepository.save(order);
        // If the new status is CONFIRMED, send an email to the customer.
        if (OrderStatus.CONFIRMED.equals(updatedOrder.getStatus())) {
            emailService.sendOrderConfirmedNotification(updatedOrder);
        }
        return new OrderResponse(updatedOrder);
    }

    // --- PROTECTED ADMIN ENDPOINTS ---

    @GetMapping("/by-restaurant/kitchen")
    public List<OrderResponse> getKitchenOrders() {
        User currentUser = getCurrentUser();
        Long restaurantId = currentUser.getRestaurant().getId();

        // The list of statuses is now fixed, not configurable.
        List<OrderStatus> statusesToFetch = List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING);
        
        List<Order> orders = orderRepository.findByRestaurantIdAndStatusIn(restaurantId, statusesToFetch);
        orders.sort(Comparator.comparing(Order::getId)); // Oldest first for the kitchen

        return orders.stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    // This is for the main Order Dashboard (history/management)
    @GetMapping("/by-restaurant")
    public List<OrderResponse> getOrdersByRestaurant() {
        User currentUser = getCurrentUser();
        Long restaurantId = currentUser.getRestaurant().getId();
        // This findByRestaurantId method should order by ID desc
        return orderRepository.findByRestaurantIdOrderByIdDesc(restaurantId).stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    // --- HELPER METHODS ---
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
