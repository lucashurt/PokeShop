package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.order.DTOs.OrderResponse;
import com.example.ecommercefull.order.DTOs.OrderStatusRequest;
import com.example.ecommercefull.order.OrderRepository;
import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderItem;
import com.example.ecommercefull.order.models.OrderStatus;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Order order;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        Business businessUser = userRepository.save(new Business("business", passwordEncoder.encode("password"), "TestStore", RoleEnum.ROLE_BUSINESS));
        User customerUser = userRepository.save(new User("customer", passwordEncoder.encode("password"), "TestCustomer", RoleEnum.ROLE_CUSTOMER));
        Business shadyBusiness = userRepository.save(new Business("shady business", passwordEncoder.encode("password"), "TestShadyBusiness", RoleEnum.ROLE_BUSINESS));

        Product product = productRepository.save(new Product("product", "product description", 19.99, 5, businessUser));
        Product product2 = productRepository.save(new Product("product2", "another product description", 25.00, 10, businessUser));

        Cart customerCart = customerUser.getCart();
        customerCart.addCartItem(new CartItem(customerCart, product, 2));
        customerCart.addCartItem(new CartItem(customerCart, product2, 1));
        cartRepository.save(customerCart);

        order = new Order(customerUser, customerCart.cartTotal());

        for (CartItem item : customerCart.getCartItems()) {
            order.addOrderItem(new OrderItem(item.getProduct(), item.getQuantity(), item.getProduct().getPrice()));
        }

        orderRepository.save(order);
    }

    @Test
    void shouldReturnOrderHistory() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext json = JsonPath.parse(response.getBody());
        int length = json.read("$.length()");
        assertThat(length).isEqualTo(1);
        JSONArray orderIds = json.read("$..orderId");
        assertThat(orderIds.size()).isEqualTo(1);
        JSONArray total = json.read("$..total");
        assertThat(total.size()).isEqualTo(1);
        assertThat(total.get(0)).isEqualTo(64.97999999999999);
        JSONArray createdAt = json.read("$..createdAt");
        assertThat(createdAt.size()).isEqualTo(1);
        assertThat(createdAt.get(0)).isNotNull();
        JSONArray updatedAt = json.read("$..updatedAt");
        assertThat(updatedAt.size()).isEqualTo(1);
        assertThat(updatedAt.get(0)).isNotNull();
    }

    @Test
    void shouldNotReturnOrderHistoryOfLoggedOutUser(){
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnSingleOrder(){
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders/" + order.getId(), OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().orderId()).isNotNull();
        assertThat(response.getBody().orderId()).isEqualTo(order.getId());
        assertThat(response.getBody().createdAt()).isNotNull();
        assertThat(response.getBody().createdAt()).isEqualTo(order.getCreatedAt());
        assertThat(response.getBody().updatedAt()).isNotNull();
        assertThat(response.getBody().updatedAt()).isEqualTo(order.getUpdatedAt());
        assertThat(response.getBody().total()).isNotNull();
        assertThat(response.getBody().total()).isEqualTo(order.getTotal());
    }
    @Test
    void shouldNotReturnNonExistentOrder(){
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .getForEntity("/api/orders/" + order.getId()+3, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldNotReturnOtherUsersOrder(){
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("business", "password")
                .getForEntity("/api/orders/" + order.getId(), OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DirtiesContext
    void shouldAllowBusinessesToChangeOrderStatus(){
        OrderStatusRequest request = new OrderStatusRequest(OrderStatus.PROCESSING);
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("business", "password")
                .postForEntity("/api/orders/" + order.getId()  +"/status", request,OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().orderStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldNotALlowCustomersToChangeOrderStatus(){
        OrderStatusRequest request = new OrderStatusRequest(OrderStatus.PROCESSING);
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/api/orders/" + order.getId()  +"/status", request,OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowNonRelatedBusinessesToChangeOrderStatus(){
        OrderStatusRequest request = new OrderStatusRequest(OrderStatus.PROCESSING);
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("shadyBusiness", "password")
                .postForEntity("/api/orders/" + order.getId()  +"/status", request,OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldCancelOrder(){
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("customer", "password")
                .exchange("/api/orders/" + order.getId()  +"/cancel", HttpMethod.DELETE,null,String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Order Cancelled");
    }

    @Test
    @DirtiesContext
    void shouldNotCancelAnotherUsersOrder(){
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("business", "password")
                .exchange("/api/orders/" + order.getId()  +"/cancel", HttpMethod.DELETE,null,String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}