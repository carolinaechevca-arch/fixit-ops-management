package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request;

import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TaskRequest(
        @NotBlank(message = "Task name is required")
        String name,

        String description,

        @NotBlank(message = "Priority is required (LOW, MEDIUM, HIGH)")
        @ValidEnum(
                enumClass = TaskPriority.class,
        message = "Priority must be LOW, MEDIUM, or HIGH")
        String priority
) {}