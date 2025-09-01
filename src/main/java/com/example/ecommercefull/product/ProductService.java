package com.example.ecommercefull.product;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.product.DTOs.ProductRequest;
import com.example.ecommercefull.product.DTOs.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private ProductRepository productRepository;
    private UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ProductResponse createProduct(ProductRequest productRequest,String businessUsername) {
        Business business = (Business)userRepository.findByUsername(businessUsername)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        Product product = new Product(
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                productRequest.stock(),
                business
        );
        productRepository.save(product);
        return ProductResponse.fromEntity(product);
    }

    public ProductResponse findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    public List<ProductResponse> findAll(){
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }
}
