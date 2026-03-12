package com.fixit_ops_management.infraestructure.adapters.driving.rest.controller;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TaskRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TaskResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper.ITaskRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for creating and assigning maintenance tasks")
public class TaskController {

    private final ITaskServicePort taskServicePort;
    private final ITaskRestMapper taskRestMapper;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskRestMapper.toResponse(
                        taskServicePort.createTask(taskRestMapper.toDomain(request))
                ));
    }

}