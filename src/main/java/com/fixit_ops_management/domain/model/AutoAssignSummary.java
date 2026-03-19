package com.fixit_ops_management.domain.model;

import com.fixit_ops_management.domain.util.constants.DomainConstants;
import lombok.Builder;

@Builder
public record AutoAssignSummary(
                long assignedCount,
                long remainingPendingCount,
                boolean success,
                String message) {

    public static AutoAssignSummary buildEmptySummary() {
        return AutoAssignSummary.builder()
                .assignedCount(0)
                .remainingPendingCount(0)
                .success(true)
                .message(DomainConstants.NO_PENDING_URGENT_TASKS_MESSAGE)
                .build();
    }
    public static AutoAssignSummary buildFinalSummary(long assigned, int remaining) {
        boolean allDone = (remaining == 0);
        String message = allDone
                ? DomainConstants.ALL_URGENT_TASKS_ASSIGNED_MESSAGE
                : String.format(DomainConstants.AUTO_ASSIGN_URGENT_TASKS_MESSAGE, assigned, remaining);

        return AutoAssignSummary.builder()
                .assignedCount(assigned)
                .remainingPendingCount(remaining)
                .success(allDone)
                .message(message)
                .build();
    }
}
