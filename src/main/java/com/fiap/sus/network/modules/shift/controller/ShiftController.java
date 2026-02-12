package com.fiap.sus.network.modules.shift.controller;

import com.fiap.sus.network.modules.shift.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;

import com.fiap.sus.network.modules.shift.dto.ShiftResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

    @GetMapping
    public ResponseEntity<Page<ShiftResponse>> listShifts(
            @RequestParam UUID unitId,
            Pageable pageable) {
        Page<ShiftResponse> page = service.listShifts(unitId, pageable);
        if (page.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid ShiftUpdateRequest dto) {
        service.updateShift(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule")
    public ResponseEntity<Void> defineSchedule(@RequestBody @Valid ShiftScheduleRequest dto) {
        service.defineSchedule(dto);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
