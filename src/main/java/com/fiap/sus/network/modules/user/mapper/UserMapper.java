package com.fiap.sus.network.modules.user.mapper;

import com.fiap.sus.network.modules.user.dto.UserResponse;
import com.fiap.sus.network.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import com.fiap.sus.network.modules.user.dto.RoleResponse;

@Component
@RequiredArgsConstructor
public class UserMapper {
    
    private final RoleMapper roleMapper;

    public UserResponse toDto(User entity) {
        if (entity == null) return null;
        List<RoleResponse> encodedRoles = entity.getGlobalRoles().stream().map(roleMapper::toDto).toList();
        return new UserResponse(entity.getId(), entity.getName(), entity.getCpfCnpj(), encodedRoles);
    }
}
