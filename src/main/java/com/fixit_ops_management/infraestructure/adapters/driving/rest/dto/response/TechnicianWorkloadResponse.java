package com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record TechnicianWorkloadResponse(
        String status,
        Integer availablePoints,
        List<TaskSummaryResponse> assignedTasks
) {}