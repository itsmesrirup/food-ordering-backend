package com.dass.foodordering.food_ordering_backend.controller;

import com.dass.foodordering.food_ordering_backend.dto.request.OrderItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.request.OrderRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderItemResponse;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderResponse;
import com.dass.foodordering.food_ordering_backend.exception.ResourceNotFoundException;
import com.dass.foodordering.food_ordering_backend.model.Customer;
import com.dass.foodordering.food_ordering_backend.model.MenuItem;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.OrderItem;
import com.dass.foodordering.food_ordering_backend.model.OrderStatus;
import com.dass.foodordering.food_ordering_backend.model.Restaurant;
import com.dass.foodordering.food_ordering_backend.model.User;
import com.dass.foodordering.food_ordering_backend.repository.CustomerRepository;
import com.dass.foodordering.food_ordering_backend.repository.MenuItemRepository;
import com.dass.foodordering.food_ordering_backend.repository.OrderRepository;
import com.dass.foodordering.food_ordering_backend.repository.RestaurantRepository;
import com.dass.foodordering.food_ordering_backend.service.EmailService;
import com.dass.foodordering.food_ordering_backend.exception.BadRequestException;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private RestaurantRepository restaurantRepository;

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
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
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
                .findFirst().orElseThrow(); // Should not happen due to our earlier check
                
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            order.addOrderItem(orderItem); // Use the helper method
            
            totalPrice += menuItem.getPrice() * itemRequest.getQuantity();
        }
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
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

    @GetMapping("/byRestaurant/{restaurantId}")
    public List<OrderResponse> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        // You might want to add a check to ensure the restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        
        return orderRepository.findByRestaurantId(restaurantId).stream() // We need to create this method
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }
}
