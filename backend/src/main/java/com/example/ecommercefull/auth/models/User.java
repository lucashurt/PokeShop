package com.example.ecommercefull.auth.models;


import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.order.models.Order;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Cart cart;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private Set<Order> orders;

    public User() {
        this.cart = new Cart();
        cart.setUser(this);
    }

    public User(String username, String password, String fullName, RoleEnum role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.cart = new Cart();
        this.cart.setUser(this);
    }

    public Long getId() {return id;}

    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getFullName() {return fullName;}
    public RoleEnum getRole() {return role;}
    public Cart getCart() {return cart;}
    public void setCart(Cart cart) {this.cart = cart;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setFullName(String fullName) {this.fullName = fullName;}
    public void setRole(RoleEnum role) {this.role = role;}
}
