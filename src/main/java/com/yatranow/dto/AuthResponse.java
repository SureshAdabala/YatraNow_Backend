package com.yatranow.dto;

public record AuthResponse(
        String token,
        String role,
        String name,
        String email,
        Long id) {
}
