package com.skillforge.backend.service;

import com.skillforge.backend.dto.AuthResponse;
import com.skillforge.backend.dto.LoginRequest;
import com.skillforge.backend.dto.RegisterRequest;
import com.skillforge.backend.exception.BadRequestException;
import com.skillforge.backend.model.AppUser;
import com.skillforge.backend.model.Role;
import com.skillforge.backend.repository.UserRepository;
import com.skillforge.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (request.role() != Role.STUDENT && request.role() != Role.INSTRUCTOR) {
            throw new BadRequestException("Only STUDENT and INSTRUCTOR roles are allowed.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        AppUser user = AppUser.builder()
                .name(request.name())
                .username(request.email())
                .email(request.email())
                .password(encodedPassword)
                .legacyPassword(encodedPassword)
                .role(request.role())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(
                User.withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRole().name()).build(),
                Map.of("role", user.getRole().name(), "name", user.getName(), "uid", user.getId())
        );

        return new AuthResponse(token, token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        String token = jwtService.generateToken(
                User.withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRole().name()).build(),
                Map.of("role", user.getRole().name(), "name", user.getName(), "uid", user.getId())
        );

        return new AuthResponse(token, token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
