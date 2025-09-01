package com.example.ecommercefull.cart.DTOs;

import com.example.ecommercefull.cart.model.CartItem;

import java.util.List;

public record CartResponse(Long cartId, List<CartItem> cartItems, int subtotal) {
}
