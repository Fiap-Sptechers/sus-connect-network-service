package com.fiap.sus.network.modules.shift.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import com.fiap.sus.network.modules.specialty.entity.Specialty;

public record ShiftUpdate(
    @NotNull Long unitId,
    @NotNull String specialty,
    @PositiveOrZero Integer activeDoctors,
    @PositiveOrZero Integer waitingPatients
) {}