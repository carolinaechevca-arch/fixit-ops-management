package com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper;

import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TaskRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.AutoAssignResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ITaskRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "technicianId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    Task toDomain(TaskRequest request);

    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);

    default TaskPriority mapPriority(String priority) {
        return TaskPriority.valueOf(priority.toUpperCase());
    }

    @Mapping(target = "pendingCount", source = "remainingPendingCount")
    @Mapping(target = "status", expression = "java(summary.success() ? \"SUCCESS\" : \"PARTIAL\")")
    AutoAssignResponse toAutoAssignResponse(AutoAssignSummary summary);
}