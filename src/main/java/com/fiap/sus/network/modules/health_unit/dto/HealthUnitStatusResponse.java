package com.fiap.sus.network.modules.health_unit.dto;

import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import java.util.List;

import java.util.UUID;

public record HealthUnitStatusResponse(
    UUID id,
    String name,
    List<ShiftResponse> activeShifts
) {}
