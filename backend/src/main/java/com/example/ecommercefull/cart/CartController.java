package com.example.ecommercefull.cart;

import com.example.ecommercefull.cart.DTOs.CartItemRequest;
import com.example.ecommercefull.cart.DTOs.CartResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @GetMapping
    public ResponseEntity<CartResponse> getCartItems(Authentication authentication) {
        String username = authentication.getName();
        CartResponse cartResponse = cartService.getCart(username);
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addProductToCart(@RequestBody CartItemRequest request, Authentication authentication) {
        String username = authentication.getName();
        CartResponse cartResponse = cartService.addProductToCart(username,request.productId(),request.quantity());
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/remove")
    public ResponseEntity<CartResponse> removeProductFromCart(@RequestBody CartItemRequest request, Authentication authentication) {
        String username = authentication.getName();
        CartResponse cartResponse = cartService.removeProductFromCart(username,request.productId());
        return ResponseEntity.ok(cartResponse);
    }
}
