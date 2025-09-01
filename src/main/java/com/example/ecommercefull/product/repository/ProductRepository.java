package com.example.ecommercefull.product.repository;

import com.example.ecommercefull.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
