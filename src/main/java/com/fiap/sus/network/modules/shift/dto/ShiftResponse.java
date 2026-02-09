package com.fiap.sus.network.modules.shift.dto;

import com.fiap.sus.network.modules.specialty.dto.SpecialtyResponse;
import com.fiap.sus.network.modules.doctor.dto.DoctorResponse;

import java.util.Set;

import java.util.UUID;

public record ShiftResponse(
    UUID id,
    UUID unitId,
    SpecialtyResponse specialty,
    Integer capacity,
    Integer activeDoctorsCount,
    Set<DoctorResponse> doctors
) {}
