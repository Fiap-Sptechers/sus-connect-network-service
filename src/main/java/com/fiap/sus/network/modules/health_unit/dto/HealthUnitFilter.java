package com.fiap.sus.network.modules.health_unit.dto;

import com.fiap.sus.network.modules.health_unit.enums.DistanceUnit;

public record HealthUnitFilter(
    String name,
    String state,
    String city,
    String baseAddress,
    Double radius,
    DistanceUnit distanceUnit
) {}
