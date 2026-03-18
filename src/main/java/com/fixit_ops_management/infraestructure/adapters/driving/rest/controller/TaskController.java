package com.fixit_ops_management.infraestructure.adapters.driving.rest.controller;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TaskRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.AutoAssignResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.DeleteResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TaskResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper.ITaskRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for creating and assigning maintenance tasks")
public class TaskController {

        private final ITaskServicePort taskServicePort;
        private final ITaskRestMapper taskRestMapper;

        @PostMapping
        @Operation(summary = "Create a new task with automatic assignment", description = "Creates a new task and automatically assigns it to an available technician. The system selects the best technician based on skill and availability.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid data in the request")
        })
        public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(taskRestMapper.toResponse(
                                                taskServicePort.create(taskRestMapper.toDomain(request))));
        }

        @GetMapping
        @Operation(summary = "List all tasks", description = "Gets the complete list of all tasks in the system")
        @ApiResponse(responseCode = "200", description = "Task list successfully obtained")
        public ResponseEntity<List<TaskResponse>> getAllTasks() {
                return ResponseEntity.ok(
                                taskRestMapper.toResponseList(taskServicePort.getAll()));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get details of a task", description = "Gets detailed information about a specific task by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task found"),
                        @ApiResponse(responseCode = "404", description = "Task not found")
        })
        public ResponseEntity<TaskResponse> getTaskById(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id) {
                return ResponseEntity.ok(
                                taskRestMapper.toResponse(taskServicePort.getById(id)));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a task", description = "Deletes a task with business rule validation. Does not allow deleting tasks in IN_PROGRESS or COMPLETED status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task deleted successfully", content = @Content(schema = @Schema(implementation = DeleteResponse.class))),
                        @ApiResponse(responseCode = "400", description = "The task cannot be deleted (invalid status)"),
                        @ApiResponse(responseCode = "404", description = "Task not found")
        })
        public ResponseEntity<DeleteResponse> deleteTask(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id) {
                taskServicePort.delete(id);
                return ResponseEntity.ok(DeleteResponse.createDeleteResponse(id));
        }

        @PostMapping("/{id}/assign-urgent")
        @Operation(summary = "Assign urgent task to Master (Manual)", description = "Assigns a specific urgent task to the Master with the least load of urgent tasks. Scenario 1: Manual assignment control.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task assigned successfully to Master", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "The task is not urgent"),
                        @ApiResponse(responseCode = "404", description = "Task not found"),
                        @ApiResponse(responseCode = "500", description = "No Master technicians available")
        })
        public ResponseEntity<TaskResponse> assignUrgentTask(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id) {
                return ResponseEntity.status(HttpStatus.OK)
                                .body(taskRestMapper.toResponse(taskServicePort.assignUrgentTask(id)));
        }

        @PostMapping("/auto-assign/urgent")
        @Operation(summary = "Auto-assign all pending urgent tasks")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Auto-assignment completed"),
                @ApiResponse(responseCode = "500", description = "No Master technicians available")
        })
        public ResponseEntity<AutoAssignResponse> autoAssignUrgentTasks() {
                return ResponseEntity.ok(
                        taskRestMapper.toAutoAssignResponse(taskServicePort.autoAssignAllUrgentTasks())
                );
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update task", description = "Updates name, description and priority of a task, applying reassignment rules.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Task updated successfully"),
                @ApiResponse(responseCode = "404", description = "Task not found"),
                @ApiResponse(responseCode = "500", description = "No Master technicians available")
        })
        public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                       @Valid @RequestBody TaskRequest request) {
                return ResponseEntity.ok(
                        taskRestMapper.toResponse(
                                taskServicePort.updateTask(id, taskRestMapper.toDomain(request))
                        )
                );
        }

        //RF13

        @PostMapping("/process-waiting")
        @Operation(summary = "Process waiting tasks", description = "Processes all tasks in WAITING status and attempts to assign them according to technician availability.")
        @ApiResponse(responseCode = "200", description = "Waiting tasks processed successfully")
        public ResponseEntity<String> processWaitingTasks() {
                taskServicePort.processWaitingTasks();
                return ResponseEntity.ok("Waiting tasks processed successfully");
        }

        //RF14
        @PatchMapping("/{id}/start")
        @Operation(summary = "Start task", description = "Changes the task status from ASSIGNED to IN_PROGRESS")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Task started successfully"),
                @ApiResponse(responseCode = "404", description = "Task not found")
        })
        public ResponseEntity<String> startTask(@PathVariable Long id) {
                taskServicePort.startTask(id);
                return ResponseEntity.ok("Task started successfully");
        }

        @PatchMapping("/{id}/complete")
        @Operation(summary = "Complete task", description = "Marks the task as completed and records the closing date automatically")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Task completed successfully"),
                @ApiResponse(responseCode = "404", description = "Task not found")
        })
        public ResponseEntity<String> completeTask(@PathVariable Long id) {
                taskServicePort.completeTask(id);
                return ResponseEntity.ok("Task completed successfully");
        }


}