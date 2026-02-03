package com.fiap.sus.network.modules.health_unit.dto;

import com.fiap.sus.network.modules.health_unit.entity.ContactType;

import java.util.UUID;

public record ContactResponse(
    UUID id,
    ContactType type,
    String value,
    String description
) {}
