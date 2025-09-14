package com.example.ecommercefull.auth.DTOs;

public record AuthResponse(String message,String token,String role, Long userId) {
    public AuthResponse(String message){
        this(message,null,null,null);
    }

    public AuthResponse(String message,String token,String role, Long userId) {
        this.message = message;
        this.token = token;
        this.role = role;
        this.userId = userId;
    }
}
