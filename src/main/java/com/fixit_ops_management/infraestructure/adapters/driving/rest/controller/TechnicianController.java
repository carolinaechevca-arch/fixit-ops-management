package com.fixit_ops_management.infraestructure.adapters.driving.rest.controller;

import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TechnicianRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper.ITechnicianRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final ITechnicianServicePort technicianServicePort;
    private final ITechnicianRestMapper technicianRestMapper;

    @PostMapping
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody TechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(technicianRestMapper.toResponse(technicianServicePort.createTechnician(
                        technicianRestMapper.toDomain(request)
                )));

    }

    @GetMapping
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        return ResponseEntity.ok(
                technicianServicePort.getAllTechnicians()
                        .stream()
                        .map(technicianRestMapper::toResponse)
                        .toList()
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(
                technicianRestMapper.toResponse(
                        technicianServicePort.getTechnicianById(id)
                )
        );
    }

}
