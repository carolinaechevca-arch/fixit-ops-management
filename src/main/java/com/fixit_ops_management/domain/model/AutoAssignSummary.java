package com.fixit_ops_management.domain.model;

import lombok.Builder;

@Builder
public record AutoAssignSummary(
                long assignedCount,
                long remainingPendingCount,
                boolean success,
                String message) {
}
