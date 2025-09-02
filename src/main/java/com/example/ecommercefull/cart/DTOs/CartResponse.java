package com.example.ecommercefull.cart.DTOs;

import com.example.ecommercefull.cart.models.Cart;

import java.util.List;

public record CartResponse(Long cartId, List<CartItemResponse> cartItems, Double subtotal) {
    public static CartResponse fromEntity(Cart cart){
        List<CartItemResponse> cartItems = cart.getCartItems()
                .stream()
                .map(CartItemResponse::fromEntity)
                .toList();
        return new CartResponse(
                cart.getId(),
                cartItems,
                cart.cartTotal()
        );
    }
}
