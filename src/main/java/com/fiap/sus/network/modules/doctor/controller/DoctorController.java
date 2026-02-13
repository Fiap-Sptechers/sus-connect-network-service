package com.fiap.sus.network.modules.doctor.controller;

import com.fiap.sus.network.modules.doctor.dto.DoctorRequest;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;
import com.fiap.sus.network.modules.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService service;

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> listDoctors(
            @RequestParam(required = false) UUID unitId,
            Pageable pageable) {
        Page<DoctorResponse> page;
        if (unitId != null) {
            page = service.listByUnitId(unitId, pageable);
        } else {
            page = service.listByUnitId(null, pageable);
        }
        
        if (page.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody @Valid DoctorRequest request) {
        return ResponseEntity.status(201).body(service.createDoctor(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
