package com.memora.backend.service;

import com.memora.backend.dto.request.LoginRequest;
import com.memora.backend.dto.request.RegisterRequest;
import com.memora.backend.dto.response.AuthResponseV2;
import com.memora.backend.entity.User;
import com.memora.backend.exception.DuplicateContentException;
import com.memora.backend.exception.MemoraServiceException;
import com.memora.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@memora.com");
        registerRequest.setPassword("password123");
        registerRequest.setDisplayName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@memora.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_happyPath() {
        UUID userId = UUID.randomUUID();
        User savedUser = User.builder()
                .id(userId)
                .email("test@memora.com")
                .emailHash("hash")
                .displayName("Test User")
                .build();

        when(userRepository.findByEmailHash(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateToken(any(), any())).thenReturn("fake-token");
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");

        AuthResponseV2 response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("fake-token");
        assertThat(response.getEmail()).isEqualTo("test@memora.com");
        assertThat(response).isNotNull();
    }

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepository.findByEmailHash(any())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        UUID userId = UUID.randomUUID();
        PasswordEncoder realEncoder = new BCryptPasswordEncoder();
        String hashedPassword = realEncoder.encode("password123");

        User user = User.builder()
                .id(userId)
                .email("test@memora.com")
                .emailHash("hash")
                .passwordHash(hashedPassword)
                .displayName("Test User")
                .build();

        when(userRepository.findByEmailHash(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("fake-token");

        AuthResponseV2 response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-token");
    }

    @Test
    void login_wrongPassword_throws401() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .emailHash("hash")
                .passwordHash("hashed")
                .build();

        when(userRepository.findByEmailHash(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(MemoraServiceException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_emailNotFound_throws401() {
        when(userRepository.findByEmailHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(MemoraServiceException.class)
                .hasMessageContaining("Invalid credentials");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deleteAccount_deletesUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        authService.deleteAccount(userId);

        verify(userRepository).delete(user);
    }
}