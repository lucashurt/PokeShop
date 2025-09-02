package com.example.ecommercefull.product;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.product.DTOs.ProductRequest;
import com.example.ecommercefull.product.DTOs.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ProductResponse createProduct(ProductRequest productRequest,String businessUsername) {
        System.out.println("== createProduct START ==");
        System.out.println("businessUsername = " + businessUsername);
        System.out.println("productRequest = " + productRequest); // record toString or build manually

        System.out.println("userRepository == null? " + (userRepository == null));
        System.out.println("productRepository == null? " + (productRepository == null));

        User user = userRepository.findByUsername(businessUsername)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if(!(user instanceof  Business)){
            throw new IllegalStateException("User is not a Business");
        }
        Business business = (Business)user;
        Product product = new Product();
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setStock(productRequest.stock());
        product.setBusiness(business);
        business.addProduct(product);

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    public ProductResponse findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    public List<ProductResponse> findAll(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!(user instanceof  Business)){
            throw new IllegalStateException("User is not a Business");
        }
        Business business = (Business)user;

        return productRepository.findAllByBusinessId(business.getId())
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }
}
