package com.fiap.sus.network.modules.user.dto;

import java.util.UUID;

public record MemberRequest(
    UUID userId,
    String roleName // e.g. OPERATOR
) {}
