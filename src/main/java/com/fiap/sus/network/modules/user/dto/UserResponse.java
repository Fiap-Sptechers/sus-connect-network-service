package com.fiap.sus.network.modules.user.dto;

import java.util.List;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String cpfCnpj,
    List<RoleResponse> globalRoles
) {}
