package com.example.ecommercefull.order;

import com.example.ecommercefull.order.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserUsername(String username);
    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);
}
