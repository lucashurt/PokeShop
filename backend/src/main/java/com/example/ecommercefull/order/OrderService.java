package com.example.ecommercefull.order;

import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.order.DTOs.OrderResponse;
import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<OrderResponse> getAllOrders(String username) {
        return orderRepository.findByUserUsername(username)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId,String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
        if(!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to order");
        }
        return OrderResponse.fromEntity(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus orderStatus, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
        boolean owner = order.getOrderItems().stream()
                        .anyMatch(item -> item.getProduct().getBusiness().getUsername().equals(username));
        if(!owner) {
            throw new RuntimeException("Unauthorized access to order");
        }
        order.setOrderStatus(orderStatus);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));
        if(!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to order");
        }
        if(order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot cancel order");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
