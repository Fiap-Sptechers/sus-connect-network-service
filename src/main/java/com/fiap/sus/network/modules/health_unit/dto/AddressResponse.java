package com.fiap.sus.network.modules.health_unit.dto;

public record AddressResponse(
    String street,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state,
    String zipCode
) {}
