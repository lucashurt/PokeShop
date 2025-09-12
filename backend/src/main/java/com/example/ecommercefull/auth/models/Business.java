package com.example.ecommercefull.auth.models;

import com.example.ecommercefull.product.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Business extends User {
    @OneToMany(mappedBy = "business",cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public Business() {}
    public Business(String username, String password, String fullName, RoleEnum role) {
        super(username, password, fullName, role);
    }

    public List<Product> getProducts() {return products;}

    public void addProduct(Product product) {
        products.add(product);
        product.setBusiness(this);
    }
    public void removeProduct(Product product) {
        products.remove(product);
        product.setBusiness(null);
    }
}
