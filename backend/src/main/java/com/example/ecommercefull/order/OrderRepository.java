package com.example.ecommercefull.order;

import com.example.ecommercefull.order.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserUsername(String username);
    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);
    @Query("select o from Order o join fetch o.user")
    List<Order> findAllWithUser();

}
