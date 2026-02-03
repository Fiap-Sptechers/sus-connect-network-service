package com.fiap.sus.network.modules.health_unit.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
    @NotBlank String street,
    @NotBlank String number,
    String complement,
    @NotBlank String neighborhood,
    @NotBlank String city,
    @NotBlank String state,
    @NotBlank String zipCode
) {
    public String toFormattedString() {
        return String.format("%s, %s, %s - %s, %s - %s", 
            street, number, neighborhood, city, state, "Brasil");
    }
}
