package com.fiap.sus.network.modules.user.mapper;

import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
         RoleMapper rm = mock(RoleMapper.class);
         mapper = new UserMapper(rm);
    }

    @Test
    void toDto_ShouldMapFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Name");
        user.setCpfCnpj("CPF");
        
        UserResponse response = mapper.toDto(user);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getName(), response.name());
        assertEquals(user.getCpfCnpj(), response.cpfCnpj());
    }
}
