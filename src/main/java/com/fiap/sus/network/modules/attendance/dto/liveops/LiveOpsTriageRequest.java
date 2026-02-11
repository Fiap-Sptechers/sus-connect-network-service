package com.fiap.sus.network.modules.attendance.dto.liveops;

import com.fiap.sus.network.modules.attendance.enums.RiskClassification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LiveOpsTriageRequest(
        @NotNull(message = "Health Unit ID is required")
        UUID healthUnitId,

        @NotBlank(message = "Patient name is required")
        String patientName,

        @NotBlank(message = "Patient document is required")
        String patientCpf,

        @NotNull(message = "Risk classification is required")
        RiskClassification riskClassification
) {}