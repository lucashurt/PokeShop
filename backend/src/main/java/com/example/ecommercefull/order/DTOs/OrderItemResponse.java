package com.example.ecommercefull.order.DTOs;

import com.example.ecommercefull.order.models.OrderItem;

public record OrderItemResponse(Long orderId, String productName, int quantity, Double price) {
    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }
}
