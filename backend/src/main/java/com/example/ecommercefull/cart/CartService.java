package com.example.ecommercefull.cart;

import com.example.ecommercefull.cart.DTOs.CartResponse;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.product.Product;
import com.example.ecommercefull.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart(String username) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(()-> new RuntimeException("cart not found"));
        return CartResponse.fromEntity(cart);
    }

    public CartResponse addProductToCart(String username, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(()-> new RuntimeException("cart not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new RuntimeException("product not found"));
        CartItem cartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(new CartItem(cart,product,0));
        if(quantity > product.getStock()){
            throw new RuntimeException("not enough stock");
        }
        int newQuantity = cartItem.getQuantity() + quantity;
        cartItem.setQuantity(newQuantity);
        product.setStock(product.getStock() - quantity);
        if(cartItem.getId() == null){
            cart.getCartItems().add(cartItem);
        }
        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    public CartResponse removeProductFromCart(String username, Long productId) {
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(()-> new RuntimeException("cart not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new RuntimeException("product not found"));
        Optional<CartItem> cartItem = cart
                .getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (cartItem.isEmpty()) {
            throw new IllegalStateException("product not present in cart");
        }
        product.setStock(product.getStock() + cartItem.get().getQuantity());
        cart.getCartItems().remove(cartItem.get());
        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }
}
