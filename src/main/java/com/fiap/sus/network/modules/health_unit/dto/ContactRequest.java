package com.fiap.sus.network.modules.health_unit.dto;

import com.fiap.sus.network.modules.health_unit.entity.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactRequest(
    @NotNull ContactType type,
    @NotBlank String value,
    String description
) {}
