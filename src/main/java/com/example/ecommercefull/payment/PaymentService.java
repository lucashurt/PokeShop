package com.example.ecommercefull.payment;

import com.example.ecommercefull.auth.models.User;
import com.example.ecommercefull.auth.repositories.UserRepository;
import com.example.ecommercefull.cart.CartRepository;
import com.example.ecommercefull.cart.models.Cart;
import com.example.ecommercefull.cart.models.CartItem;
import com.example.ecommercefull.order.OrderRepository;
import com.example.ecommercefull.order.models.Order;
import com.example.ecommercefull.order.models.OrderItem;
import com.example.ecommercefull.order.models.OrderStatus;
import com.example.ecommercefull.payment.DTOs.CheckoutRequest;
import com.example.ecommercefull.payment.DTOs.CheckoutResponse;
import com.example.ecommercefull.payment.DTOs.PaymentRequest;
import com.example.ecommercefull.payment.DTOs.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session; import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;


    @Value("${stripe.api.secretKey}")
    private String stripesSecretKey;

    public PaymentService(UserRepository userRepository, CartRepository cartRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponse createPaymentIntent(String username, PaymentRequest paymentRequest) {
        try{
            Stripe.apiKey = stripesSecretKey;

            Cart cart = cartRepository.findByUserUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            if(cart.getCartItems().isEmpty()){
                return new PaymentResponse(null,"failed","Cart is empty");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = new Order(user,cart.cartTotal());

            for(CartItem cartItem: cart.getCartItems()){
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
                    .putMetadata("order_id",savedOrder.getId().toString())
                    .putMetadata("user_id",user.getId().toString())
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            savedOrder.setStripePaymentIntentId(paymentIntent.getId());
            orderRepository.save(savedOrder);

            return new PaymentResponse(
                    paymentIntent.getClientSecret(),
                    "requires_payment_method",
                    "Payment intent created succesfully"
            );
        }
        catch(StripeException e){
            return new PaymentResponse(null,"failed","Payment failed: " + e.getMessage());
        }
    }
    public CheckoutResponse createCheckoutSession(String username, CheckoutRequest request) {
        try {
            Stripe.apiKey = stripesSecretKey;

            // Get user's cart
            Cart cart = cartRepository.findByUserUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            // Create line items for Stripe
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

            Session session = Session.create(params);

            return new CheckoutResponse(session.getId(), session.getUrl());

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage());
        }
    }

    public void handlePaymentStatus(String paymentIntentId, String status) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (status.toLowerCase()) {
            case "succeeded" -> {
                order.setOrderStatus(OrderStatus.PAID);

                Cart cart = cartRepository.findByUserUsername(order.getUser().getUsername())
                        .orElse(null);
                if (cart != null) {
                    cart.getCartItems().clear();
                    cartRepository.save(cart);
                }
            }
            case "payment_failed" -> order.setOrderStatus(OrderStatus.FAILED);
            case "canceled" -> order.setOrderStatus(OrderStatus.CANCELLED);
            case "processing" -> order.setOrderStatus(OrderStatus.PROCESSING);
            default -> throw new RuntimeException("Unknown payment status: " + status);
        }

        orderRepository.save(order);
    }
}
