package com.fiap.sus.network.modules.attendance.dto;


import com.fiap.sus.network.modules.attendance.enums.AttendanceStatus;

public record AttendanceResponse(
        String id,
        String patientName,
        AttendanceStatus status,
        String entryTime
) {
}
