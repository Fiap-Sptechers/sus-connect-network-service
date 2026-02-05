package com.fiap.sus.network.modules.user.mapper;

import com.fiap.sus.network.modules.user.dto.RoleResponse;
import com.fiap.sus.network.modules.user.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapper();

    @Test
    void toDto_ShouldMapEntityToDto() {
        Role entity = new Role();
        entity.setName("ADMIN");
        entity.setLevel(1);

        RoleResponse dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("ADMIN", dto.name());
        assertEquals(1, dto.level());
    }

    @Test
    void toDto_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(mapper.toDto(null));
    }
}
