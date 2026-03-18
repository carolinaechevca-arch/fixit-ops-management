package com.fixit_ops_management.infraestructure.adapters.driving.rest.controller;

import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TechnicianRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianWorkloadResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper.ITechnicianRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
@Tag(name = "Technician Management", description = "Endpoints for registering and managing maintenance personnel")
public class TechnicianController {

    private final ITechnicianServicePort technicianServicePort;
    private final ITechnicianRestMapper technicianRestMapper;

    @PostMapping
    @Operation(summary = "Register a new technician", description = "Creates a new technician with a specific category (JUNIOR, SEMI_SENIOR, SENIOR, MASTER). Validates DNI uniqueness.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Technician created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid category"),
            @ApiResponse(responseCode = "409", description = "id already exists")
    })
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(technicianRestMapper.toResponse(technicianServicePort.create(
                        technicianRestMapper.toDomain(request)
                )));
    }

    @GetMapping
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        return ResponseEntity.ok(
                technicianServicePort.getAll()
                        .stream()
                        .map(technicianRestMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(
                technicianRestMapper.toResponse(
                        technicianServicePort.getById(id)
                )
        );
    }


    @GetMapping("/{id}/workload")
    @Operation(summary = "Get technician workload and capacity")
    public ResponseEntity<TechnicianWorkloadResponse> getTechnicianWorkload(@PathVariable Long id) {
      TechnicianWorkload workload = technicianServicePort.getTechnicianWorkload(id);
      return ResponseEntity.ok(technicianRestMapper.toWorkloadResponse(workload));

    }

    @PatchMapping("/{id}/category")
    @Operation(summary = "Update technician category",
            description = "Changes the category of a technician only if they are currently AVAILABLE and have no tasks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Technician is busy or has tasks"),
            @ApiResponse(responseCode = "404", description = "Technician not found")
    })
    public ResponseEntity<TechnicianResponse> updateCategory(
            @PathVariable Long id,
            @RequestParam TechnicianCategory newCategory) {

        Technician updated = technicianServicePort.updateCategory(id, newCategory);
        return ResponseEntity.ok(technicianRestMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a technician",
            description = "Deletes a technician only if their status is AVAILABLE (RF-04).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Technician deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Technician is busy or not available"),
            @ApiResponse(responseCode = "404", description = "Technician not found")
    })
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        technicianServicePort.delete(id);
        return ResponseEntity.noContent().build();
    }
}
