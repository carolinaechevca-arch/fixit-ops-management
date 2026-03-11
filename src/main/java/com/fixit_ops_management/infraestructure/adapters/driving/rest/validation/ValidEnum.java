package com.fixit_ops_management.infraestructure.adapters.driving.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "Invalid value for category";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}