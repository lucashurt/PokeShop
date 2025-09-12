package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.cart.DTOs.CartItemRequest;
import com.example.ecommercefull.cart.DTOs.CartResponse;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.product.Product;
import com.example.ecommercefull.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Product product;
    private Product product3;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        Business businessUser = userRepository.save(new Business("business", passwordEncoder.encode("password"), "TestStore", RoleEnum.ROLE_BUSINESS));
        User customerUser = userRepository.save(new User("customer", passwordEncoder.encode("password"), "TestCustomer", RoleEnum.ROLE_CUSTOMER));


        product = productRepository.save(new Product("product", "product description", 19.99, 5, businessUser));
        Product product2 = productRepository.save(new Product("product2", "another product description", 25.00, 10, businessUser));
        product3 = productRepository.save(new Product("product3", "yet another product description", 39.99, 5, businessUser));


        Cart customerCart = customerUser.getCart();
        customerCart.addCartItem(new CartItem(customerCart, product, 2));
        customerCart.addCartItem(new CartItem(customerCart, product2, 1));
        cartRepository.save(customerCart);
    }

    @Test
    void shouldReturnCart() {
        ResponseEntity<CartResponse> cartResponse = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/cart", CartResponse.class);
        assertThat(cartResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CartResponse cartResponseBody = cartResponse.getBody();
        assertThat(cartResponseBody.cartId()).isNotNull();
        assertThat(cartResponseBody.cartItems()).hasSize(2);
        assertThat(cartResponseBody.subtotal()).isEqualTo(19.99 * 2 + 25.00);
        assertThat(cartResponseBody.cartItems().get(0).productName()).isEqualTo("product");
        assertThat(cartResponseBody.cartItems().get(1).productName()).isEqualTo("product2");
        assertThat(cartResponseBody.cartItems().get(0).quantity()).isEqualTo(2);
        assertThat(cartResponseBody.cartItems().get(1).quantity()).isEqualTo(1);
        assertThat(cartResponseBody.cartItems().get(0).price()).isEqualTo(19.99);
        assertThat(cartResponseBody.cartItems().get(1).price()).isEqualTo(25.00);
    }

    @Test
    void shouldNotReturnCartToNoAuth() {
        ResponseEntity<CartResponse> cartResponse = restTemplate
                .getForEntity("/cart", CartResponse.class);
        assertThat(cartResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldAddProductToCart() {
        CartItemRequest request = new CartItemRequest(product3.getId(),2);
        ResponseEntity<CartResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/cart/add", request, CartResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().cartItems()).hasSize(3);
        assertThat(response.getBody().cartItems().get(2).productName()).isEqualTo("product3");
        assertThat(response.getBody().cartItems().get(2).quantity()).isEqualTo(2);
        assertThat(response.getBody().cartItems().get(2).price()).isEqualTo(39.99);
    }

    @Test
    void shouldNotAddNonExistentProductToCartTo() {
        CartItemRequest request = new CartItemRequest(product3.getId() + 100000,2);
        ResponseEntity<CartResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/cart/add", request, CartResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldNotAddProductToCartToNoAuth() {
        CartItemRequest request = new CartItemRequest(product3.getId(),2);
        ResponseEntity<CartResponse> response = restTemplate
                .postForEntity("/cart/add", request, CartResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldRemoveProductFromCart() {
        CartItemRequest request = new CartItemRequest(product.getId(),2);
        ResponseEntity<CartResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/cart/remove", request, CartResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().cartItems()).hasSize(1);
    }

    @Test
    void shouldNotRemoveNonExistentProductFromCartTo() {
        CartItemRequest request = new CartItemRequest(product3.getId()+1000,1);
        ResponseEntity<CartResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/cart/remove", request, CartResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
