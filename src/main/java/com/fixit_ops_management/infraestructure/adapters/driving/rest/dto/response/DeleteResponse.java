package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DeleteResponse(
                String message,
                Long resourceId,
                String status,
                LocalDateTime timestamp) {
}
