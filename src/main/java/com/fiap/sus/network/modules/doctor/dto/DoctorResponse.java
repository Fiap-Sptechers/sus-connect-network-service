package com.fiap.sus.network.modules.doctor.dto;

import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;

import java.util.Set;

import java.util.UUID;

public record DoctorResponse(
    UUID id,
    String name,
    String crm,
    Set<SpecialtyResponse> specialties
) {}
