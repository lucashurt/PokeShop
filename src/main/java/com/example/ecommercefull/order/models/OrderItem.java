package com.example.ecommercefull.order.models;

import com.example.ecommercefull.product.Product;
import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name="product_id")
    private Product product;

    private int quantity;
    private Double price;

    public OrderItem() {}
    public OrderItem(Product product, int quantity, Double price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() {return id;}
    public Order  getOrder() {return order;}
    public void setOrder(Order order) {this.order = order;}
    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}
    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public Double getPrice() {return price;}
    public void setPrice(Double price) {this.price = price;}
}
