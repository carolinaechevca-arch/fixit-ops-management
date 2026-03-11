package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

@Builder
public record TechnicianResponse(
        Long id,
        String dni,
        String name,
        String category,
        String status,
        Integer taskCount,
        Integer currentPoints
) {}