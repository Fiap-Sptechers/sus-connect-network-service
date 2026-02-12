package com.fiap.sus.network.modules.shift.dto;

import com.fiap.sus.network.modules.specialty.enums.SpecialtyEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShiftUpdateRequest(
    @NotNull UUID unitId,
    @NotNull SpecialtyEnum specialty,
    @PositiveOrZero Integer capacity
) {}
