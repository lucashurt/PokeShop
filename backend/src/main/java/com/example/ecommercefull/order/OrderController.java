package com.example.ecommercefull.order;

import com.example.ecommercefull.order.DTOs.OrderResponse;
import com.example.ecommercefull.order.DTOs.OrderStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication) {
        String username = authentication.getName();
        List<OrderResponse> orders = orderService.getAllOrders(username);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, Authentication authentication) {
        String username = authentication.getName();
        OrderResponse  order = orderService.getOrderById(orderId, username);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderStatusRequest orderStatusRequest,Authentication authentication) {
        OrderResponse orderResponse = orderService.updateOrderStatus(orderId,orderStatusRequest.status(),authentication.getName());
        return ResponseEntity.ok(orderResponse);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        orderService.cancelOrder(orderId, authentication.getName());
        return ResponseEntity.ok("Order Cancelled");
    }
}
