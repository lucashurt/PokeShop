package com.example.ecommercefull.payment.DTOs;

public record PaymentResponse(String clientSecret, String status, String message) {
}
