package com.fiap.sus.network.modules.attendance.dto.liveops;


import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.network.modules.attendance.enums.RiskClassification;

import java.time.LocalDateTime;

public record LiveOpsAttendanceResponse(
        String id,
        String healthUnitId,
        String patientName,
        AttendanceStatus status,
        RiskClassification riskClassification,
        LocalDateTime entryTime
) {
}
