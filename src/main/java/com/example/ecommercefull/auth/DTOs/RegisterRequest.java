package com.example.ecommercefull.auth.DTOs;

public record RegisterRequest(String username, String password, String fullName, String role) {
}
