package com.fiap.sus.network.modules.doctor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CrmValidator implements ConstraintValidator<Crm, String> {

    // Regex: CRM/seguido de 2 letras maiúsculas (UF), espaço e 6 dígitos numéricos
    private static final Pattern CRM_PATTERN = Pattern.compile("^CRM/[A-Z]{2}\\s\\d{6}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Use @NotNull ou @NotBlank para validar nulidade
        }
        return CRM_PATTERN.matcher(value).matches();
    }
}
