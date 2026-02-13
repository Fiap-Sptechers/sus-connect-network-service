package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.modules.user.dto.LoginRequest;
import com.fiap.sus.network.modules.user.dto.TokenResponse;
import com.fiap.sus.network.modules.user.dto.UserRequest;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.core.exception.ResourceAlreadyExistsException;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import com.fiap.sus.network.core.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiap.sus.network.modules.user.entity.Role;
import com.fiap.sus.network.modules.user.repository.RoleRepository;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
       User user = userRepository.findByCpfCnpj(request.cpfCnpj())
           .orElseThrow(() -> new ResourceNotFoundException("User not found"));
           
       if (!passwordEncoder.matches(request.password(), user.getPassword())) {
           throw new ResourceNotFoundException("Invalid credentials");
       }
       
       String token = tokenService.generateToken(user);
       return new TokenResponse(token);
    }

    public TokenResponse refresh() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpfCnpj = auth.getName();
        
        User user = userRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String newToken = tokenService.generateToken(user);
        return new TokenResponse(newToken);
    }

    @Transactional
    public void createUser(UserRequest request) {
        if (userRepository.findByCpfCnpj(request.cpfCnpj()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with this CPF/CNPJ already exists");
        }
        
        if (request.admin()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));
            
            if (!isAdmin) {
                throw new AccessDeniedException("Only SUPER_ADMIN users can create other admins.");
            }
        }

        User user = new User();
        user.setName(request.name());
        user.setCpfCnpj(request.cpfCnpj());
        user.setPassword(passwordEncoder.encode(request.password()));
        
        if (request.admin()) {
            Role adminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role SUPER_ADMIN not found. Please run migrations."));
            user.getGlobalRoles().add(adminRole);
        }
        
        userRepository.save(user);
    }
}
