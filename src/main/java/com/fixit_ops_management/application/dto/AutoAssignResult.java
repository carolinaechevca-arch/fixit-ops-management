package com.fixit_ops_management.application.dto;

import lombok.Builder;

@Builder
public record AutoAssignResult(
                long assignedCount,
                long remainingPendingCount,
                boolean success,
                String message) {
}
