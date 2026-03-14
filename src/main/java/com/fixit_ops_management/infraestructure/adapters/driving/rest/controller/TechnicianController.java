package com.fixit_ops_management.infraestructure.adapters.driving.rest.controller;

import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TechnicianRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper.ITechnicianRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
                .body(technicianRestMapper.toResponse(technicianServicePort.createTechnician(
                        technicianRestMapper.toDomain(request)
                )));
    }
}