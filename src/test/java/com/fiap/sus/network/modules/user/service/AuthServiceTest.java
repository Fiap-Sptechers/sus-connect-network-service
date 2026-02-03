package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.core.exception.ResourceAlreadyExistsException;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.core.security.TokenService;
import com.fiap.sus.network.modules.user.dto.LoginRequest;
import com.fiap.sus.network.modules.user.dto.TokenResponse;
import com.fiap.sus.network.modules.user.dto.UserRequest;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    // We need to inject real encoder or mock it. AuthService uses new BCryptPasswordEncoder() directly in field declaration?
    // Let's check AuthService source. It does `private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();`
    // So we can't easily mock it unless we verify behavior or use Reflection to swap it, but usually standard encoder is fine.

    @BeforeEach
    void setUp() {
        // No special setup needed
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        String cpf = "12345678900";
        String password = "password";
        String encodedPassword = new BCryptPasswordEncoder().encode(password);
        
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpfCnpj(cpf);
        user.setPassword(encodedPassword);

        when(userRepository.findByCpfCnpj(cpf)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("valid_token");

        LoginRequest request = new LoginRequest(cpf, password);

        // Act
        TokenResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("valid_token", response.token());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest("invalid", "pass");
        when(userRepository.findByCpfCnpj("invalid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void createUser_ShouldSaveUser_WhenCpfDoesNotExist() {
        UserRequest request = new UserRequest("Name", "pass", "12345678900");
        when(userRepository.findByCpfCnpj(request.cpfCnpj())).thenReturn(Optional.empty());

        authService.createUser(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenCpfExists() {
        UserRequest request = new UserRequest("Name", "pass", "12345678900");
        when(userRepository.findByCpfCnpj(request.cpfCnpj())).thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.createUser(request));
    }
}
