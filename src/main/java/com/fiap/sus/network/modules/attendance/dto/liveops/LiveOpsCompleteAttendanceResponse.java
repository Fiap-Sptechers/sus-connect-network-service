package com.fiap.sus.network.modules.attendance.dto.liveops;


import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.network.modules.attendance.enums.RiskClassification;

import java.time.LocalDateTime;

public record LiveOpsCompleteAttendanceResponse(
        String id,
        String healthUnitId,
        String patientName,
        String patientCpf,
        AttendanceStatus status,
        RiskClassification riskClassification,
        LocalDateTime entryTime,
        LocalDateTime startTime,
        LocalDateTime dischargeTime
) {
}
