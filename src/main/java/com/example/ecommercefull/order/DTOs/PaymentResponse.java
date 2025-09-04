package com.example.ecommercefull.order.DTOs;

public record PaymentResponse(String clientSecret, String status, String message) {
}
