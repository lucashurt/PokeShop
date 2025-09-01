package com.example.ecommercefull.cart.models;

import com.example.ecommercefull.auth.models.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    public Cart() {}
    public Cart(User user) {
        this.user = user;
    }
    public Long getId() {return id;}
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    public List<CartItem> getCartItems(){return cartItems;}
    public void addCartItem(CartItem cartItem){cartItems.add(cartItem);}
    public Double cartTotal(){
       return cartItems.stream()
               .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
               .sum();
    }
}
