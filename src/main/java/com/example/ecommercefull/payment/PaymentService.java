package com.example.ecommercefull.payment;

import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.order.OrderRepository;
import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderItem;
import com.example.ecommercefull.order.models.OrderStatus;
import com.example.ecommercefull.payment.DTOs.CheckoutRequest;
import com.example.ecommercefull.payment.DTOs.CheckoutResponse;
import com.example.ecommercefull.payment.DTOs.PaymentRequest;
import com.example.ecommercefull.payment.DTOs.PaymentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.api.secretKey}")
    private String stripesSecretKey;

    public PaymentService(UserRepository userRepository,
                          CartRepository cartRepository,
                          OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponse createPaymentIntent(String username, PaymentRequest paymentRequest) {
        try {
            Stripe.apiKey = stripesSecretKey;

            Cart cart = cartRepository.findByUserUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            if (cart.getCartItems().isEmpty()) {
                return new PaymentResponse(null, "failed", "Cart is empty");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = new Order(user, cart.cartTotal());
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem(
                        cartItem.getProduct(),
                        cartItem.getQuantity(),
                        cartItem.getProduct().getPrice()
                );
                order.addOrderItem(orderItem);
            }
            Order savedOrder = orderRepository.save(order);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (cart.cartTotal() * 100))
                    .setCurrency(paymentRequest.currency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("order_id", savedOrder.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .build();

            com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent.create(params);

            savedOrder.setStripePaymentIntentId(paymentIntent.getId());
            orderRepository.save(savedOrder);

            return new PaymentResponse(
                    paymentIntent.getClientSecret(),
                    "requires_payment_method",
                    "Payment intent created successfully"
            );
        } catch (StripeException e) {
            return new PaymentResponse(null, "failed", "Payment failed: " + e.getMessage());
        }
    }

    public CheckoutResponse createCheckoutSession(String username, CheckoutRequest request) {
        try {
            Stripe.apiKey = stripesSecretKey;

            Cart cart = cartRepository.findByUserUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = new Order(user, cart.cartTotal());
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem(
                        cartItem.getProduct(),
                        cartItem.getQuantity(),
                        cartItem.getProduct().getPrice()
                );
                order.addOrderItem(orderItem);
            }
            Order savedOrder = orderRepository.save(order);

            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            for (CartItem cartItem : cart.getCartItems()) {
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(cartItem.getProduct().getName())
                                                        .setDescription(cartItem.getProduct().getDescription())
                                                        .build()
                                        )
                                        .setUnitAmount((long) (cartItem.getProduct().getPrice() * 100))
                                        .build()
                        )
                        .setQuantity((long) cartItem.getQuantity())
                        .build();
                lineItems.add(lineItem);
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", savedOrder.getId().toString());
            metadata.put("user_id", user.getId().toString());
            if (request.metadata() != null) {
                metadata.putAll(request.metadata());
            }

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.successUrl())
                    .setCancelUrl(request.cancelUrl())
                    .addAllLineItem(lineItems)
                    .putAllMetadata(metadata)
                    .build();

            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(params);

            savedOrder.setStripePaymentIntentId(session.getId());
            orderRepository.save(savedOrder);

            return new CheckoutResponse(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage());
        }
    }

    @Transactional
    public void handleStripeEventRaw(Event event, String rawPayload) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawPayload);
            JsonNode dataObj = root.path("data").path("object");

            JsonNode metadataNode = dataObj.path("metadata");
            if (!metadataNode.isMissingNode()) {
                metadataNode.fieldNames().forEachRemaining(key ->
                        System.out.println("metadata key: " + key + " -> " + metadataNode.get(key).asText())
                );
            }

            JsonNode paymentIntentNode = dataObj.path("payment_intent");
            System.out.println("payment_intent node: " + (paymentIntentNode.isMissingNode() ? "missing" : paymentIntentNode.asText()));
            JsonNode idNode = dataObj.path("id");
            System.out.println("object id node: " + (idNode.isMissingNode() ? "missing" : idNode.asText()));

            String orderId = null;
            if (!metadataNode.isMissingNode() && metadataNode.has("order_id") && !metadataNode.get("order_id").isNull()) {
                orderId = metadataNode.get("order_id").asText();
            }

            String paymentIntentId = null;
            if (!paymentIntentNode.isMissingNode() && !paymentIntentNode.isNull()) {
                paymentIntentId = paymentIntentNode.asText();
            }

            String objectId = null;
            if (!idNode.isMissingNode() && !idNode.isNull()) {
                objectId = idNode.asText();
            }

            System.out.println("Parsed orderId=" + orderId + " paymentIntentId=" + paymentIntentId + " objectId=" + objectId);

            if (orderId != null) {
                try {
                    long parsed = Long.parseLong(orderId);
                    System.out.println("orderRepository.existsById(" + parsed + ") = " + orderRepository.existsById(parsed));
                } catch (Exception ex) {
                    System.err.println("Could not parse orderId: " + orderId);
                }
            }

            Optional<Order> orderOptional = Optional.empty();
            if (orderId != null) {
                try {
                    orderOptional = orderRepository.findById(Long.valueOf(orderId));
                    if (orderOptional.isPresent()) System.out.println("Found order by metadata order_id=" + orderId);
                } catch (Exception ex) {
                    System.err.println("Error finding order by metadata id: " + ex.getMessage());
                }
            }

            if (orderOptional.isEmpty() && paymentIntentId != null) {
                orderOptional = orderRepository.findByStripePaymentIntentId(paymentIntentId);
                if (orderOptional.isPresent()) System.out.println("Found order by paymentIntentId=" + paymentIntentId);
            }

            if (orderOptional.isEmpty() && objectId != null) {
                orderOptional = orderRepository.findByStripePaymentIntentId(objectId);
                if (orderOptional.isPresent()) System.out.println("Found order by objectId=" + objectId);
            }

            List<Order> recent = orderRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
            System.out.println("Recent orders (id | stripePaymentIntentId):");
            recent.forEach(o -> System.out.println(o.getId() + " | " + o.getStripePaymentIntentId()));

            if (orderOptional.isEmpty()) {
                System.err.println("Order not found for event. type=" + event.getType() +
                        ", parsed orderId=" + orderId + ", paymentIntent=" + paymentIntentId + ", objectId=" + objectId);
                return;
            }

            Order order = orderOptional.get();
            System.out.println("Processing order id=" + order.getId() + " currentStatus=" + order.getOrderStatus());

            switch (event.getType()) {
                case "checkout.session.completed":
                    if (paymentIntentId != null) {
                        order.setStripePaymentIntentId(paymentIntentId);
                        System.out.println("Saved payment_intent " + paymentIntentId + " to order " + order.getId());
                    } else {
                        if (objectId != null && objectId.startsWith("cs_")) {
                            order.setStripePaymentIntentId(objectId);
                        }
                    }
                    order.setOrderStatus(OrderStatus.PAID);
                    clearUserCart(order.getUser().getUsername());
                    break;

                case "payment_intent.succeeded":
                    order.setOrderStatus(OrderStatus.PAID);

                    if (paymentIntentId != null) order.setStripePaymentIntentId(paymentIntentId);
                    clearUserCart(order.getUser().getUsername());
                    break;

                case "payment_intent.canceled":
                case "checkout.session.expired":
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    break;

                case "payment_intent.processing":
                    order.setOrderStatus(OrderStatus.PROCESSING);
                    break;

                default:
                    System.out.println("Unhandled event type: " + event.getType());
                    return;
            }

            orderRepository.save(order);
            System.out.println("Saved order id=" + order.getId() + " newStatus=" + order.getOrderStatus());

        } catch (Exception e) {
            System.err.println("Exception in handleStripeEventRaw: " + e.getMessage());
        }
    }

    private void clearUserCart(String username) {
        try {
            Optional<Cart> cartOptional = cartRepository.findByUserUsername(username);
            if (cartOptional.isPresent()) {
                Cart cart = cartOptional.get();
                cart.getCartItems().clear();
                cartRepository.save(cart);
                System.out.println("Cart cleared for user: " + username);
            } else {
                System.out.println("No cart found to clear for user: " + username);
            }
        } catch (Exception e) {
            System.out.println("Exception in handleStripeEventRaw: " + e.getMessage());
        }
    }
}
