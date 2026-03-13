package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AutoAssignResponse(
                long assignedCount,
                long pendingCount,
                String message,
                String status) {
}
