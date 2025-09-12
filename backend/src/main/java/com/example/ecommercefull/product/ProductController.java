package com.example.ecommercefull.product;

import com.example.ecommercefull.product.DTOs.ProductRequest;
import com.example.ecommercefull.product.DTOs.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long requestedId) {
        return ResponseEntity.ok(productService.findById(requestedId));
    }

    @GetMapping("/{requestedBusiness}/inventory")
    public ResponseEntity<List<ProductResponse>> getProducts(@PathVariable String requestedBusiness) {
        List<ProductResponse> products = productService.findAll(requestedBusiness);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest, Authentication authentication) {
        String username = authentication.getName();
        ProductResponse response = productService.createProduct(productRequest, username);
        return ResponseEntity.ok(response);
    }
}
