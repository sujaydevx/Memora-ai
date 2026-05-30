package com.memora.backend.service;

import com.memora.backend.dto.AuthRequest;
import com.memora.backend.dto.AuthResponse;
import com.memora.backend.entity.User;
import com.memora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private String hashEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing email", e);
        }
    }

    public AuthResponse register(AuthRequest request) {
        String emailHash = hashEmail(request.getEmail());
        if (userRepository.existsByEmailHash(emailHash)) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .emailHash(emailHash)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getFullName())
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(request.getEmail());
        return new AuthResponse(token, request.getEmail(), request.getFullName());
    }

    public AuthResponse login(AuthRequest request) {
        String emailHash = hashEmail(request.getEmail());
        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
        String token = jwtService.generateToken(request.getEmail());
        return new AuthResponse(token, request.getEmail(), user.getDisplayName());
    }
}