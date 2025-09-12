package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Business;
import com.example.ecommercefull.auth.models.RoleEnum;
import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.models.*;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.order.OrderRepository;
import com.example.ecommercefull.order.models.*;
import com.example.ecommercefull.payment.DTOs.*;
import com.example.ecommercefull.product.Product;
import com.example.ecommercefull.product.ProductRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customerUser;
    private Product product;
    private Product product2;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();

        Business businessUser = userRepository.saveAndFlush(
                new Business("business", passwordEncoder.encode("password"), "TestStore", RoleEnum.ROLE_BUSINESS)
        );

        customerUser = userRepository.saveAndFlush(
                new User("customer", passwordEncoder.encode("password"), "TestCustomer", RoleEnum.ROLE_CUSTOMER)
        );

        product = productRepository.save(
                new Product("product", "product description", 19.99, 5, businessUser)
        );

        product2 = productRepository.save(
                new Product("product2", "another product description", 25.00, 10, businessUser)
        );

        Cart customerCart = customerUser.getCart();
        customerCart.addCartItem(new CartItem(customerCart, product, 2));
        customerCart.addCartItem(new CartItem(customerCart, product2, 1));
        cartRepository.save(customerCart);
    }

    @Test
    @DirtiesContext
    void shouldCreatePaymentIntent() {
        PaymentRequest request = new PaymentRequest("pm_card_visa", "usd");

        ResponseEntity<PaymentResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/payment/create-intent", request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PaymentResponse paymentResponse = response.getBody();

        assertThat(paymentResponse.clientSecret()).isNotNull();
        assertThat(paymentResponse.clientSecret()).contains("_secret_");
        assertThat(paymentResponse.status()).isNotNull();
        assertThat(paymentResponse.message()).isEqualTo("Payment intent created successfully");
        assertThat(orderRepository.count()).isGreaterThan(0);

        Order createdOrder = orderRepository.findAllWithUser().get(0);
        assertThat(createdOrder.getUser().getUsername()).isEqualTo("customer");
        assertThat(createdOrder.getTotal()).isEqualTo(64.97999999999999);
    }

    @Test
    @DirtiesContext
    void shouldNotCreatePaymentIntentWithEmptyCart() {
        User emptyCartUser = userRepository.saveAndFlush(
                new User("emptycustomer", passwordEncoder.encode("password"), "EmptyCartCustomer", RoleEnum.ROLE_CUSTOMER)
        );

        PaymentRequest request = new PaymentRequest("pm_card_visa", "usd");

        ResponseEntity<PaymentResponse> response = restTemplate
                .withBasicAuth("emptycustomer", "password")
                .postForEntity("/payment/create-intent", request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().clientSecret()).isNull();
        assertThat(response.getBody().status()).isEqualTo("failed");
        assertThat(response.getBody().message()).isEqualTo("Cart is empty");
    }

    @Test
    void shouldNotCreatePaymentIntentForUnauthenticatedUser() {
        PaymentRequest request = new PaymentRequest("pm_card_visa", "usd");

        ResponseEntity<PaymentResponse> response = restTemplate
                .postForEntity("/payment/create-intent", request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldCreateCheckoutSession() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("customer_id", "cus_123");

        CheckoutRequest request = new CheckoutRequest(
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                metadata
        );

        ResponseEntity<CheckoutResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/payment/create-checkout-session", request, CheckoutResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        CheckoutResponse checkoutResponse = response.getBody();

        assertThat(checkoutResponse.sessionId()).isNotNull();
        assertThat(checkoutResponse.sessionId()).startsWith("cs_");
        assertThat(checkoutResponse.url()).isNotNull();
        assertThat(checkoutResponse.url()).contains("checkout.stripe.com");
        assertThat(orderRepository.count()).isGreaterThan(0);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateCheckoutSessionWithEmptyCart() {
        User emptyCartUser = userRepository.saveAndFlush(
                new User("emptycustomer2", passwordEncoder.encode("password"), "EmptyCartCustomer2", RoleEnum.ROLE_CUSTOMER)
        );

        CheckoutRequest request = new CheckoutRequest(
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                null
        );

        ResponseEntity<CheckoutResponse> response = restTemplate
                .withBasicAuth("emptycustomer2", "password")
                .postForEntity("/payment/create-checkout-session", request, CheckoutResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldNotCreateCheckoutSessionForUnauthenticatedUser() {
        CheckoutRequest request = new CheckoutRequest(
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                null
        );

        ResponseEntity<CheckoutResponse> response = restTemplate
                .postForEntity("/payment/create-checkout-session", request, CheckoutResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectWebhookWithoutSignature() {
        String webhookPayload = createWebhookPayload("payment_intent.succeeded", "pi_test_123", "123");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/payment/webhook", webhookPayload, String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectWebhookWithInvalidSignature() {
        String webhookPayload = createWebhookPayload("payment_intent.succeeded", "pi_test_123", "123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Stripe-Signature", "invalid_signature");
        HttpEntity<String> request = new HttpEntity<>(webhookPayload, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/payment/webhook", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DirtiesContext
    void shouldCreateOrderWhenPaymentIntentCreatedLooser() {
        PaymentRequest request = new PaymentRequest("pm_card_visa", "usd");

        ResponseEntity<PaymentResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/payment/create-intent", request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Order> userOrders = orderRepository.findByUserUsername("customer");
        assertThat(userOrders).isNotEmpty();

        Order createdOrder = userOrders.get(0);
        assertThat(createdOrder.getTotal()).isEqualTo(64.97999999999999);
        assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }


    @Test
    @DirtiesContext
    void shouldCreateOrderWhenCheckoutSessionCreated() {
        CheckoutRequest request = new CheckoutRequest(
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                null
        );

        long initialOrderCount = orderRepository.count();

        ResponseEntity<CheckoutResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/payment/create-checkout-session", request, CheckoutResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderRepository.count()).isEqualTo(initialOrderCount + 1);

        Order createdOrder = orderRepository.findByUserUsername("customer")
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createdOrder.getTotal()).isEqualTo(64.97999999999999);
        assertThat(createdOrder.getStripePaymentIntentId()).isNotNull();
    }

    @Test
    @DirtiesContext
    void shouldHandleStripeAPIErrors() {
        PaymentRequest request = new PaymentRequest("invalid_pm", "usd");

        ResponseEntity<PaymentResponse> response = restTemplate
                .withBasicAuth("customer", "password")
                .postForEntity("/payment/create-intent", request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("requires_payment_method");
    }

    private String createWebhookPayload(String eventType, String paymentIntentId, String orderId) {
        Map<String, Object> webhook = new HashMap<>();
        webhook.put("type", eventType);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        object.put("id", paymentIntentId);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("order_id", orderId);
        object.put("metadata", metadata);

        data.put("object", object);
        webhook.put("data", data);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(webhook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }
    }
}
