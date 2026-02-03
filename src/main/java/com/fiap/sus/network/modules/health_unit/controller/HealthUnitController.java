package com.fiap.sus.network.modules.health_unit.controller;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitFilter;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.service.HealthUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fiap.sus.network.modules.health_unit.dto.HealthUnitStatusResponse;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class HealthUnitController {

    private final HealthUnitService service;

    @PostMapping
    public ResponseEntity<HealthUnitResponse> create(@RequestBody HealthUnitRequest request) {
        HealthUnitResponse created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<HealthUnitResponse>> list(
            @ModelAttribute HealthUnitFilter filter, 
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthUnitResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<HealthUnitStatusResponse> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getStatus(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<Page<HealthUnitResponse>> listNearby(
            @ModelAttribute HealthUnitFilter filter, 
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(filter, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
