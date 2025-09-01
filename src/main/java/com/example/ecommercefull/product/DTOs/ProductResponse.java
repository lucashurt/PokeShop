package com.example.ecommercefull.product.DTOs;

import com.example.ecommercefull.product.model.Product;

public record ProductResponse(Long id, String name, String description, Double price, int stock, String businessName) {
    public ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getBusiness().getFullName()
        );
    }
}
