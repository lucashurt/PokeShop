package com.example.ecommercefull.payment;

import com.example.ecommercefull.payment.DTOs.CheckoutRequest;
import com.example.ecommercefull.payment.DTOs.CheckoutResponse;
import com.example.ecommercefull.payment.DTOs.PaymentRequest;
import com.example.ecommercefull.payment.DTOs.PaymentResponse;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @RequestBody PaymentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        PaymentResponse response = paymentService.createPaymentIntent(username, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CheckoutResponse response = paymentService.createCheckoutSession(username, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        // Webhook handling implementation
        return ResponseEntity.ok("Webhook received");
    }
}
