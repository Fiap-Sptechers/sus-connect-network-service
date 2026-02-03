package com.fiap.sus.network.modules.shift.dto;

import com.fiap.sus.network.modules.specialty.enums.SpecialtyEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fiap.sus.network.modules.doctor.validation.Crm;
import java.util.List;

import java.util.UUID;

public record ShiftScheduleRequest(
    @NotNull UUID unitId,
    @NotNull SpecialtyEnum specialty,
    @NotEmpty List<@Crm String> doctorCrms
) {}
