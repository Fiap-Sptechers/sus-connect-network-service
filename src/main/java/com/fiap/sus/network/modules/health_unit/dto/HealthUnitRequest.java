package com.fiap.sus.network.modules.health_unit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HealthUnitRequest(
    @NotBlank String name,
    @NotBlank String cnpj,
    @NotNull @Valid AddressRequest address,
    List<@Valid ContactRequest> contacts
) {}
