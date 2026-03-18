package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DeleteResponse(
                String message,
                Long resourceId,
                String status,
                LocalDateTime timestamp) {

    public static DeleteResponse createDeleteResponse(Long id) {
        return DeleteResponse.builder()
                .message("Task deleted successfully")
                .resourceId(id)
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
