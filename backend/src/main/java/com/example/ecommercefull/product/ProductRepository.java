package com.example.ecommercefull.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByBusinessId(Long businessId);
    List<Product> findAll();
}
