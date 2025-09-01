package com.example.ecommercefull.cart.repository;

import com.example.ecommercefull.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
