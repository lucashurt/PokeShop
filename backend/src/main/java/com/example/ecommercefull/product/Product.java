package com.example.ecommercefull.product;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.cart.models.CartItem;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private int stock;

    @ManyToOne
    @JoinColumn(name="business_id")
    private Business business;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    public Product() {}
    public Product(String name, String description, Double price, int stock, Business business) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.business = business;
    }

    public Long getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public Double getPrice() {return price;}
    public int getStock() {return stock;}
    public Business getBusiness() {return business;}
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(Double price) {this.price = price;}
    public void setStock(int stock) {this.stock = stock;}
    public void setBusiness(Business business) {this.business = business;}
}
