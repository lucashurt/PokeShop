package com.example.ecommercefull.payment;

import com.example.ecommercefull.payment.DTOs.CheckoutRequest;
import com.example.ecommercefull.payment.DTOs.CheckoutResponse;
import com.example.ecommercefull.payment.DTOs.PaymentRequest;
import com.example.ecommercefull.payment.DTOs.PaymentResponse;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

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
        if(stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webgook secret is not configured");
        }

        if(signature == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST ).body("Missing Stripe-Signature header");
        }
        Event event;

        try{
            event = Webhook.constructEvent(payload,signature,stripeWebhookSecret);
        }
        catch (SignatureVerificationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Stripe-Signature ");
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }
        paymentService.handleStripeEventRaw(event,payload);
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
}
