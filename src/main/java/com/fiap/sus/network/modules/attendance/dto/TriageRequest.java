package com.fiap.sus.network.modules.attendance.dto;

import com.fiap.sus.network.modules.attendance.enums.RiskClassification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TriageRequest(
        @NotBlank(message = "Patient name is required")
        String patientName,

        @NotBlank(message = "Patient document is required")
        String patientCpf,

        @NotNull(message = "Risk classification is required")
        RiskClassification riskClassification
) {}