package com.fiap.sus.network.modules.user.mapper;

import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private UserMapper mapper;

    @Test
    void toDto_ShouldMapFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Tester");
        user.setCpfCnpj("123");
        user.setGlobalRoles(new HashSet<>());

        UserResponse response = mapper.toDto(user);

        assertNotNull(response);
        assertEquals(user.getName(), response.name());
        assertEquals(user.getCpfCnpj(), response.cpfCnpj());
    }

    @Test
    void toDto_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toDto(null));
    }
}
