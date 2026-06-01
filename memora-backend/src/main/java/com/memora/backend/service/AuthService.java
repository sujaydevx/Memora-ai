package com.memora.backend.service;

import com.memora.backend.dto.AuthRequest;
import com.memora.backend.dto.AuthResponse;
import com.memora.backend.dto.request.LoginRequest;
import com.memora.backend.dto.request.RegisterRequest;
import com.memora.backend.dto.response.AuthResponseV2;
import com.memora.backend.entity.User;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.exception.MemoraServiceException;
import com.memora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private String hashEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing email", e);
        }
    }

    public AuthResponseV2 register(RegisterRequest request) {
        String emailHash = hashEmail(request.getEmail());
        if (userRepository.findByEmailHash(emailHash).isPresent()) {
            throw new DuplicateContentException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .emailHash(emailHash)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponseV2(token, saved.getId(), request.getEmail(), request.getDisplayName());
    }

    public AuthResponseV2 login(LoginRequest request) {
        String emailHash = hashEmail(request.getEmail());
        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new MemoraServiceException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new MemoraServiceException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponseV2(token, user.getId(), request.getEmail(), user.getDisplayName());
    }

    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemoraServiceException("User not found"));
        userRepository.delete(user);
    }
}