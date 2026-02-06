package com.fiap.sus.network.modules.health_unit.dto;

import java.util.List;

import java.util.UUID;

public record HealthUnitResponse(
    UUID id,
    String name,
    String cnpj,
    AddressResponse address,
    List<ContactResponse> contacts,
    String distance
) {}
