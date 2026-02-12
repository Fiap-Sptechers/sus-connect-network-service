package com.fiap.sus.network.modules.attendance.dto;


import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;
import com.fiap.sus.network.modules.attendance.enums.RiskClassification;

public record CompleteAttendanceResponse(
        String id,
        String patientName,
        String patientCpf,
        AttendanceStatus status,
        RiskClassification riskClassification,
        String entryTime,
        String startTime,
        String dischargeTime
) {
}
