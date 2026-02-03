package com.fiap.sus.network.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

public record UserRequest(
    @NotBlank
    String name,
    @NotBlank
    String password,
    @NotBlank
    String cpfCnpj
) {}
