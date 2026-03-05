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
import com.dass.foodordering.food_ordering_backend.model.Role;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CommissionLedgerRepository;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;
import com.dass.foodordering.food_ordering_backend.service.EmailService;
import com.dass.foodordering.food_ordering_backend.service.FeatureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.Stripe;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import com.stripe.exception.StripeException;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    @Autowired private FeatureService featureService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${stripe.api.key}")
    private String apiKey;

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
    @Transactional
    public OrderResponse createOrder(@RequestBody OrderRequest request) throws JsonProcessingException {
        
        // 1. Authenticate & Identify Role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStaffOrder = auth != null && 
                               auth.getPrincipal() instanceof User && 
                               (((User)auth.getPrincipal()).getRole() == Role.ADMIN || 
                                ((User)auth.getPrincipal()).getRole() == Role.WAITER);

        // 2. Validate Items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item.");
        }
        
        List<Long> menuItemIds = request.getItems().stream().map(OrderItemRequest::getMenuItemId).collect(Collectors.toList());
        List<MenuItem> menuItems = menuItemRepository.findAllById(menuItemIds);
        
        if (menuItems.size() != menuItemIds.size()) {
            throw new ResourceNotFoundException("One or more menu items not found.");
        }

        Restaurant restaurant = menuItems.get(0).getRestaurant();
        boolean allFromSameRestaurant = menuItems.stream()
                .allMatch(item -> item.getRestaurant().getId().equals(restaurant.getId()));

        if (!allFromSameRestaurant) {
            throw new BadRequestException("All menu items must belong to the same restaurant.");
        }

        // --- CORE LOGIC START ---
        // We declare 'order' but DO NOT initialize to null. 
        // Java enforces that we must assign it in both branches of the if/else below.
        Order order; 
        boolean isNewOrder;

        // Step A: Check for existing order (Only for Staff + Table Number)
        Optional<Order> existingOrderOpt = Optional.empty();
        
        if (isStaffOrder && request.getTableNumber() != null && !request.getTableNumber().isEmpty()) {
            List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP);
            existingOrderOpt = orderRepository.findFirstByRestaurantIdAndTableNumberAndStatusIn(
                restaurant.getId(), 
                request.getTableNumber(), 
                activeStatuses
            );
        }

        // Step B: Initialize 'order' based on result
        if (existingOrderOpt.isPresent()) {
            // --- APPEND PATH ---
            order = existingOrderOpt.get();
            isNewOrder = false;
            // No need to set customer, pickup time, or sequence (already exist)
        } else {
            // --- NEW ORDER PATH ---
            order = new Order();
            isNewOrder = true;
            
            order.setRestaurant(restaurant);
            order.setOrderTime(LocalDateTime.now());
            order.setTableNumber(request.getTableNumber());

            // Customer
            if (request.getCustomerId() != null) {
                Customer customer = customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: "+request.getCustomerId()));
                order.setCustomer(customer);
            }

            // Status
            if (isStaffOrder) {
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                order.setStatus(OrderStatus.PENDING);
            }

            // Sequence
            Long currentMax = orderRepository.getMaxOrderSequence(restaurant.getId());
            order.setRestaurantOrderSequence(currentMax + 1);

            // Pickup Time
            if (request.getPickupTime() != null) {
                if (request.getPickupTime().isBefore(LocalDateTime.now())) {
                    throw new BadRequestException("Pickup time cannot be in the past.");
                }
                order.setPickupTime(request.getPickupTime());
            } else {
                order.setPickupTime(null); // ASAP
            }

            // QR Code Check
            if (request.getTableNumber() != null && !request.getTableNumber().isEmpty()) {
                if (!isStaffOrder) {
                    if (!featureService.isFeatureAvailable(restaurant, "QR_ORDERING")) {
                        throw new AccessDeniedException("QR Code Ordering is not available for this restaurant's plan.");
                    }
                    if (!restaurant.isQrCodeOrderingEnabled()) {
                        throw new BadRequestException("QR Code ordering is not enabled for this restaurant.");
                    }
                }
            }
        } 
        // --- CORE LOGIC END ---

        // 3. Add Items & Calculate Total
        double itemsTotal = 0;
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItems.stream()
                .filter(mi -> mi.getId().equals(itemRequest.getMenuItemId()))
                .findFirst().orElseThrow();

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());

            if (itemRequest.getSelectedOptions() != null && !itemRequest.getSelectedOptions().isEmpty()) {
                orderItem.setSelectedOptionsFromList(itemRequest.getSelectedOptions());
            }

            order.addOrderItem(orderItem); 
            itemsTotal += menuItem.getPrice() * itemRequest.getQuantity();
        }

        order.setTotalPrice(order.getTotalPrice() + itemsTotal);

        if (request.getPaymentIntentId() != null) {
            order.setPaymentIntentId(request.getPaymentIntentId());
        }

        Order savedOrder = orderRepository.save(order);

        // --- Commission Logic ---
        Restaurant savedRestaurant = savedOrder.getRestaurant();
        
        if (savedRestaurant.getPaymentModel() == PaymentModel.COMMISSION && savedRestaurant.getCommissionRate() != null) {
            BigDecimal orderTotal = BigDecimal.valueOf(savedOrder.getTotalPrice());
            BigDecimal commissionRate = savedRestaurant.getCommissionRate();
            BigDecimal commissionAmount = orderTotal.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

            // TODO: If you add `findByOrder` to repo, you can update existing commissions here.
            // For now, to prevent crashing on duplicate keys, ONLY create ledger for NEW orders.
            if (isNewOrder) {
                CommissionLedger ledgerEntry = new CommissionLedger();
                ledgerEntry.setRestaurant(savedRestaurant);
                ledgerEntry.setOrder(savedOrder);
                ledgerEntry.setOrderTotal(orderTotal);
                ledgerEntry.setCommissionRate(commissionRate);
                ledgerEntry.setCommissionAmount(commissionAmount);
                ledgerEntry.setTransactionDate(LocalDateTime.now());
                commissionLedgerRepository.save(ledgerEntry);
            }
        }

        Order freshOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);
        OrderResponse response = new OrderResponse(freshOrder);

        // ✅ WEBSOCKET PUSH: Notify this specific restaurant's channel
        // Channel format: /topic/restaurant/{id}
        messagingTemplate.convertAndSend("/topic/restaurant/" + freshOrder.getRestaurant().getId(), response);

        if (isNewOrder) {
            emailService.sendNewOrderNotification(freshOrder);
        }

        return response;
    }

    // ✅ NEW ENDPOINT: Get Occupied Tables (For POS UI)
    @GetMapping("/active-tables")
    public List<String> getActiveTables() {
        User currentUser = getCurrentUser();
        List<OrderStatus> activeStatuses = Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP);
        
        List<Order> activeOrders = orderRepository.findByRestaurantIdAndStatusIn(
            currentUser.getRestaurant().getId(), 
            activeStatuses
        );

        // Return distinct table numbers that are not null
        return activeOrders.stream()
                .map(Order::getTableNumber)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .collect(Collectors.toList());
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
    public OrderResponse updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) throws StripeException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // --- ADDED: Refund Logic ---
        if (request.getStatus() == OrderStatus.CANCELLED) {
            // Check if it was paid online
            if (order.getPaymentIntentId() != null) {
                // Initialize Stripe (if not global)
                Stripe.apiKey = apiKey;

                // Create Refund
                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(order.getPaymentIntentId())
                        // Optional: .setAmount(500L) for partial, default is full
                        .build();

                Refund refund = Refund.create(params);
                
                // Optional: Log the refund ID in the order or ledger
                // order.setRefundId(refund.getId());
            }
        }
        
        order.setStatus(request.getStatus());
        
        Order updatedOrder = orderRepository.save(order);

        OrderResponse response = new OrderResponse(updatedOrder);

        // ✅ WEBSOCKET PUSH: Notify everyone looking at the dashboard/KDS
        messagingTemplate.convertAndSend("/topic/restaurant/" + updatedOrder.getRestaurant().getId(), response);

        // If the new status is CONFIRMED, send an email to the customer.
        if (OrderStatus.CONFIRMED.equals(updatedOrder.getStatus())) {
            emailService.sendOrderConfirmedNotification(updatedOrder);
        }
        // --- If the new status is CANCELLED, send an email to the customer. ---
        if (updatedOrder.getStatus() == OrderStatus.CANCELLED) {
            emailService.sendOrderCancelledNotification(updatedOrder);
        }
        return response;
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
