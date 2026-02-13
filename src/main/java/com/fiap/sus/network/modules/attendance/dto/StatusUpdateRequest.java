package com.fiap.sus.network.modules.attendance.dto;

import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull
        AttendanceStatus status
) {}
