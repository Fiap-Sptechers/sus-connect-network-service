package com.fiap.sus.network.modules.shift.dto;

import java.util.Set;

public record ShiftRequest(
    Long unitId,
    Long specialtyId
) {}
