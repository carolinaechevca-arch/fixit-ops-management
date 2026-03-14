package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.util.RestConstants;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TechnicianRequest(
        @NotBlank(message = RestConstants.DNI_REQUIRED)
        @Size(max = 20, message = RestConstants.DNI_SIZE)
        String dni,

        @NotBlank(message = RestConstants.NAME_REQUIRED)
        @Size(max = 100, message = RestConstants.NAME_SIZE)
        String name,

        @NotBlank(message = RestConstants.CATEGORY_REQUIRED)
        @ValidEnum(
                enumClass = TechnicianCategory.class,
                message = RestConstants.CATEGORY_VALID
        )
        String category
) {}