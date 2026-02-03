package com.fiap.sus.network.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
public record LoginRequest(@NotBlank String cpfCnpj, @NotBlank String password) {
}
