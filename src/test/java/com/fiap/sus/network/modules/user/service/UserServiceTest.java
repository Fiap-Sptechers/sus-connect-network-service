package com.fiap.sus.network.modules.user.service;

import com.fiap.sus.network.core.exception.ResourceNotFoundException;
import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.entity.User;
import com.fiap.sus.network.modules.user.mapper.UserMapper;
import com.fiap.sus.network.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService service;

    @Test
    void listUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        when(userMapper.toDto(any())).thenReturn(new UserResponse(UUID.randomUUID(), "test@user.com", "Tester", null));

        List<UserResponse> result = service.listUsers();

        assertFalse(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserResponse(id, "test@user.com", "Tester", null));

        UserResponse response = service.findById(id);

        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }
}
