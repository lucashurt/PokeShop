package com.example.ecommercefull.cart.DTOs;

import com.example.ecommercefull.cart.models.CartItem;

public record CartItemResponse(Long id, String productName, int quantity, Double price) {
    public static CartItemResponse fromEntity(CartItem cartItem) {
        return new CartItemResponse(
        cartItem.getId(),
        cartItem.getProduct().getName(),
        cartItem.getQuantity(),
        cartItem.getProduct().getPrice());
    }
}
