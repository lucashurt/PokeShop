package com.example.ecommercefull.payment.DTOs;

import java.util.Map;

public record CheckoutRequest(String successUrl, String cancelUrl, Map<String,String> metadata) {
}
