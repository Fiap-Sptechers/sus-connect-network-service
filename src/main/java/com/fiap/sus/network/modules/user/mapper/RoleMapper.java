package com.fiap.sus.network.modules.user.mapper;

import com.fiap.sus.network.modules.user.dto.RoleResponse;
import com.fiap.sus.network.modules.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    public RoleResponse toDto(Role entity) {
        if (entity == null) return null;
        return new RoleResponse(entity.getName(), entity.getLevel());
    }
}
