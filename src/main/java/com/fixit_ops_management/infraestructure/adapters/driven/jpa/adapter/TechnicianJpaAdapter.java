package com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter;

import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITechnicianEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITechnicianRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TechnicianJpaAdapter implements ITechnicianPersistencePort {

    private final ITechnicianRepository technicianRepository;
    private final ITechnicianEntityMapper technicianEntityMapper;

    @Override
    public Technician saveTechnician(Technician technician) {
        return technicianEntityMapper.toDomain(
                technicianRepository.save(technicianEntityMapper.toEntity(technician))
        );
    }

    @Override
    public Optional<Technician> findByDni(String dni) {
        return technicianRepository.findByDni(dni)
                .map(technicianEntityMapper::toDomain);
    }
}