package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TaskResponse(
        Long id,
        String name,
        String description,
        String priority,
        String status,
        Long technicianId,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {}
