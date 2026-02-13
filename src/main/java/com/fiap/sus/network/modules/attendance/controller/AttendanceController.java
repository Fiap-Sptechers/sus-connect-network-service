package com.fiap.sus.network.modules.attendance.controller;

import com.fiap.sus.network.modules.attendance.client.LiveOpsClient;
import com.fiap.sus.network.modules.attendance.dto.AttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.CompleteAttendanceResponse;
import com.fiap.sus.network.modules.attendance.dto.StatusUpdateRequest;
import com.fiap.sus.network.modules.attendance.dto.TriageRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.service.HealthUnitService;
import com.fiap.sus.network.modules.user.service.AccessControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/units/{unitId}/attendances")
public class AttendanceController {

    private final HealthUnitService healthUnitService;
    private final AccessControlService accessControlService;
    private final LiveOpsClient liveOpsClient;
    
    @GetMapping({"", "/"})
    public ResponseEntity<List<AttendanceResponse>> listAttendances(@PathVariable UUID unitId) {
        accessControlService.checkAccess(unitId);
        return ResponseEntity.ok(liveOpsClient.listAttendances(unitId));
    }
    
    @PostMapping
    public ResponseEntity<AttendanceResponse> startAttendance(
            @PathVariable UUID unitId,
            @RequestBody @Valid TriageRequest request
    ) {
        HealthUnitResponse unit = healthUnitService.findById(unitId);
        AttendanceResponse response = liveOpsClient.sendTriage(unit.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{attendanceId}")
    public ResponseEntity<CompleteAttendanceResponse> getAttendanceById(@PathVariable String attendanceId, @PathVariable UUID unitId) {
        accessControlService.checkAccess(unitId);
        CompleteAttendanceResponse response = liveOpsClient.getAttendanceById(attendanceId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{attendanceId}/status")
    public ResponseEntity<CompleteAttendanceResponse> updateStatus(
            @PathVariable UUID unitId,
            @PathVariable String attendanceId,
            @RequestBody @Valid StatusUpdateRequest request
    ) {
        accessControlService.checkAccess(unitId);
        CompleteAttendanceResponse response = liveOpsClient.updateStatus(attendanceId, request);
        return ResponseEntity.ok(response);
    }

}
