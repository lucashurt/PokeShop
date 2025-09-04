package com.example.ecommercefull.order.DTOs;

import java.util.Map;

public record CheckoutRequest(String successUrl, String cancelUrl, Map<String,String> metadata) {
}
