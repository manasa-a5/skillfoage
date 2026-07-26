package com.skillforge.backend.dto;

import com.skillforge.backend.model.Role;

public record AuthResponse(
        String token,
        String accessToken,
        Long userId,
        String name,
        String email,
        Role role
) {
}
