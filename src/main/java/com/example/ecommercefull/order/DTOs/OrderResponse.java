package com.example.ecommercefull.order.DTOs;

import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long orderId, Double total, OrderStatus orderStatus, LocalDateTime createdAt, LocalDateTime updatedAt,
                            List<OrderItemResponse> orderItems) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTotal(),
                order.getOrderStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .toList()
        );
    }
}
