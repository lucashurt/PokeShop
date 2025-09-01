package com.example.ecommercefull.product.model;

import com.example.ecommercefull.auth.models.Business;
import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private int quantity;

    @ManyToOne
    @JoinColumn(name="business_id")
    private Business business;

    protected Product() {}
    public Product(String name, String description, Double price, int quantity, Business business) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.business = business;
    }

    public Long getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public Double getPrice() {return price;}
    public int getQuantity() {return quantity;}
    public Business getBusiness() {return business;}
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(Double price) {this.price = price;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public void setBusiness(Business business) {this.business = business;}
}
