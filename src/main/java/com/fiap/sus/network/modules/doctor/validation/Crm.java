package com.fiap.sus.network.modules.doctor.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CrmValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Crm {
    String message() default "CRM inválido. Formato esperado: CRM/UF 123456";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
