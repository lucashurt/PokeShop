package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.order.DTOs.OrderStatusRequest;
import com.example.ecommercefull.order.OrderRepository;
import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderItem;
import com.example.ecommercefull.order.models.OrderStatus;
import com.example.ecommercefull.payment.DTOs.CheckoutRequest;
import com.example.ecommercefull.payment.DTOs.CheckoutResponse;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PaymentApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer;
    private Business business;
    private Product product;
    private Order order;

    @BeforeEach
    void setup() {
        business = userRepository.save(new Business("pizzashop", passwordEncoder.encode("password"), "Pizza Shop", RoleEnum.ROLE_BUSINESS));
        customer = userRepository.save(new User("johndoe", passwordEncoder.encode("password"), "John Doe", RoleEnum.ROLE_CUSTOMER));

        product = productRepository.save(new Product("Pepperoni Pizza", "A classic pizza", 15.99, 100, business));

        order = new Order(customer, 15.99);
        order.setOrderStatus(OrderStatus.PAID);
        OrderItem orderItem = new OrderItem(product, 1, 15.99);
        order.addOrderItem(orderItem);
        orderRepository.save(order);
    }

    @Test
    void customerCanCreateCheckoutSessionWithItems() {
        Cart cart = customer.getCart();
        cart.addCartItem(new CartItem(cart, product, 1));
        cartRepository.save(cart);

        CheckoutRequest request = new CheckoutRequest("https://example.com/success", "https://example.com/cancel", null);

        ResponseEntity<CheckoutResponse> response = restTemplate
                .withBasicAuth("johndoe", "password")
                .postForEntity("/payment/create-checkout-session", request, CheckoutResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().url()).contains("https://checkout.stripe.com");
    }

    @Test
    void customerCannotCreateCheckoutSessionWithEmptyCart() {
        CheckoutRequest request = new CheckoutRequest("https://example.com/success", "https://example.com/cancel", null);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("johndoe", "password")
                .postForEntity("/payment/create-checkout-session", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void customerCanViewTheirOwnOrders() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("johndoe", "password")
                .getForEntity("/orders", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Pepperoni Pizza");
    }

    @Test
    void customerCannotViewAnotherUsersOrder() {
        User anotherCustomer = userRepository.save(new User("janedoe", passwordEncoder.encode("password"), "Jane Doe", RoleEnum.ROLE_CUSTOMER));
        Order anotherOrder = new Order(anotherCustomer, 20.00);
        orderRepository.save(anotherOrder);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("johndoe", "password")
                .getForEntity("/orders/" + anotherOrder.getId(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void businessCanUpdateStatusOfTheirOrder() {
        // Arrange
        var request = new OrderStatusRequest(OrderStatus.SHIPPED);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("pizzashop", "password")
                .postForEntity("/orders/" + order.getId() + "/status", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Order status updated successfully");

        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
