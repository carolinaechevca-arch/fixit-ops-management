package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

@Builder
public record TaskSummaryResponse(
        Long id,
        String name,
        String priority,
        String status
) {}
