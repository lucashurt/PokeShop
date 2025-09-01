package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.Role;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.RoleRepository;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.product.DTOs.ProductRequest;
import com.example.ecommercefull.product.DTOs.ProductResponse;
import com.example.ecommercefull.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role businessRole = roleRepository.save(new Role("BUSINESS"));
        Role customerRole = roleRepository.save(new Role("CUSTOMER"));
        Business businessUser = new Business("business",passwordEncoder.encode("password"),"TestStore",businessRole);
        User customerUser = new User("customer",passwordEncoder.encode("password"),"TestCustomer",customerRole);

        userRepository.save(customerUser);
        userRepository.save(businessUser);
    }

    @Test
    public void shouldCreateProduct() {
        ProductRequest productRequest = new ProductRequest("widget","Test Widget Description",29.99,10);
        ResponseEntity<ProductResponse> productResponse = restTemplate
                .withBasicAuth("business","password")
                .postForEntity("/products", productRequest, ProductResponse.class);
        assertThat(productResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
