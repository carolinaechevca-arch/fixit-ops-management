package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TechnicianRequest(
        @NotBlank(message = "DNI is required")
        @Size(max = 20, message = "DNI must be at most 20 characters")
        String dni,
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,
        @NotBlank(message = "Category is required")
        @ValidEnum(
                enumClass = TechnicianCategory.class,
                message = "Category must be JUNIOR, SEMI_SENIOR, SENIOR, or MASTER"
        )
        String category
) {}