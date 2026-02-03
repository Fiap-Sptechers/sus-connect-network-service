package com.fiap.sus.network.modules.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import com.fiap.sus.network.modules.doctor.validation.Crm;
import java.util.Set;

import java.util.UUID;

public record DoctorRequest(
    @NotBlank String name,
    @NotBlank @Crm String crm,
    Set<UUID> specialtyIds
) {}
