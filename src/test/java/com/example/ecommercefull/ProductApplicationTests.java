package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.Role;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.RoleRepository;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.product.DTOs.ProductRequest;
import com.example.ecommercefull.product.DTOs.ProductResponse;
import com.example.ecommercefull.product.Product;
import com.example.ecommercefull.product.ProductRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

    private Product savedProduct;

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

        Product product = new Product("product","product description",19.99,5,businessUser);
        Product product2 = new Product("product2","another product description",19.99,5,businessUser);

        savedProduct = productRepository.save(product);
        productRepository.save(product2);

    }

    @Test
    public void shouldCreateProduct() {
        ProductRequest productRequest = new ProductRequest("widget","Test Widget Description",29.99,10);
        ResponseEntity<ProductResponse> productResponse = restTemplate
                .withBasicAuth("business","password")
                .postForEntity("/products", productRequest, ProductResponse.class);
        assertThat(productResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldNotAllowCustomersToCreateProduct() {
        ProductRequest productRequest = new ProductRequest("widget","Test Widget Description",29.99,10);
        ResponseEntity<ProductResponse> productResponse = restTemplate
                .withBasicAuth("customer","password")
                .postForEntity("/products", productRequest, ProductResponse.class);
        assertThat(productResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotAllowNonExistentUsersToCreateProduct() {
        ProductRequest productRequest = new ProductRequest("widget","Test Widget Description",29.99,10);
        ResponseEntity<ProductResponse> productResponse = restTemplate
                .withBasicAuth("randomUser","password")
                .postForEntity("/products", productRequest, ProductResponse.class);
        assertThat(productResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnProductToCustomer(){
        ResponseEntity<ProductResponse> response = restTemplate
                .withBasicAuth("customer","password")
                .getForEntity("/products/" + savedProduct.getId(), ProductResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(savedProduct.getId());
        assertThat(response.getBody().name()).isEqualTo("product");
        assertThat(response.getBody().description()).isEqualTo("product description");
        assertThat(response.getBody().price()).isEqualTo(19.99);
        assertThat(response.getBody().stock()).isEqualTo(5);
        assertThat(response.getBody().businessUsername()).isEqualTo("business");
    }
    @Test
    public void shouldReturnProductToBusiness(){
        ResponseEntity<ProductResponse> response = restTemplate
                .withBasicAuth("business","password")
                .getForEntity("/products/" + savedProduct.getId(), ProductResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldNotReturnNonExistingProduct(){
        ResponseEntity<ProductResponse> response = restTemplate
                .withBasicAuth("business","password")
                .getForEntity("/products/" + savedProduct.getId() + 1, ProductResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldReturnBusinessInventory(){
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("business","password")
                .getForEntity("/products/business/inventory", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext json = JsonPath.parse(response.getBody());
        int length = json.read("$.length()");
        assertThat(length).isEqualTo(2);
        JSONArray ids = json.read("$..id");
        assertThat(ids.size()).isEqualTo(2);
        JSONArray names = json.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("product","product2");
        JSONArray descriptions = json.read("$..description");
        assertThat(descriptions).containsExactlyInAnyOrder("product description","another product description");
        JSONArray prices = json.read("$..price");
        assertThat(prices).containsExactlyInAnyOrder(19.99,19.99);
        JSONArray stocks = json.read("$..stock");
        assertThat(stocks).containsExactlyInAnyOrder(5,5);
        JSONArray businessUsernames = json.read("$..businessUsername");
        assertThat(businessUsernames).containsExactlyInAnyOrder("business","business");

    }
}
