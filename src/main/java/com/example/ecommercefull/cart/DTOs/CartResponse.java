package com.example.ecommercefull.cart.DTOs;

import com.example.ecommercefull.cart.model.Cart;
import com.example.ecommercefull.cart.model.CartItem;

import java.util.List;

public record CartResponse(Long cartId, List<CartItem> cartItems, Double subtotal) {
    public CartResponse fromEntity(Cart cart){
        return new CartResponse(
                cart.getId(),
                cart.getCartItems(),
                cart.cartTotal()
        );
    }
}
