package com.example.ecommercefull.product.DTOs;

public record ProductResponse(Long id, String name, String description, Double price, int quantity,String businessName) {
}
