package com.fiap.sus.network.modules.shift.controller;

import com.fiap.sus.network.modules.shift.dto.ShiftUpdate;
import com.fiap.sus.network.modules.shift.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fiap.sus.network.modules.shift.dto.ShiftUpdateRequest;
import com.fiap.sus.network.modules.shift.dto.ShiftScheduleRequest;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

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
}
