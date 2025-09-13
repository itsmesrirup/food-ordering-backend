package com.dass.foodordering.food_ordering_backend.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dass.foodordering.food_ordering_backend.dto.request.OrderItemRequest;
import com.dass.foodordering.food_ordering_backend.dto.response.OrderItemResponse.SelectedOptionResponse;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Store the selected choices as a simple JSON string
    // e.g., "[{\"option\":\"Entrée au choix\",\"choice\":\"SAMOSSA\"}, ...]"
    @Column(columnDefinition = "TEXT")
    private String selectedOptions;

    public List<SelectedOptionResponse> getSelectedOptionsList() {
        if (selectedOptions == null) return Collections.emptyList();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(selectedOptions, SelectedOptionResponse[].class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void setSelectedOptionsFromList(List<OrderItemRequest.SelectedOptionRequest> options) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.selectedOptions = mapper.writeValueAsString(options);
        } catch (Exception e) {
            this.selectedOptions = null;
        }
    }
}
