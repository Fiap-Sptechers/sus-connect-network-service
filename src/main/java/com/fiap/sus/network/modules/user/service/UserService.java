package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        log.info("Listing all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        log.info("Finding user by id: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
