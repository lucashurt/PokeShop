package com.example.ecommercefull.cart.model;

import com.example.ecommercefull.product.model.Product;
import jakarta.persistence.*;

@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    int quantity;

    public CartItem() {}
    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {return id;}
    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public Cart getCart() {return cart;}
    public void setCart(Cart cart) {this.cart = cart;}
    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}
}
