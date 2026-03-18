package com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter;

import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITechnicianEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITechnicianRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
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

    @Override
    public List<Technician> findAllTechnicians() {
        return technicianRepository.findAll()
                .stream()
                .map(technicianEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Technician> findById(Long id) {
        return technicianRepository.findById(id)
                .map(technicianEntityMapper::toDomain);
    }

    @Override
    public List<Technician> findAll() {
        return technicianRepository.findAll().stream()
                .map(technicianEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Technician> findByCategory(TechnicianCategory category) {
        return technicianRepository.findByCategory(category)
                .stream()
                .map(technicianEntityMapper::toDomain)
                .toList();
    }
    @Override
    public List<Technician> findAllOrderedByHierarchy() {
        return technicianRepository.findAllOrderedByHierarchy()
                .stream()
                .map(technicianEntityMapper::toDomain)
                .toList();
    }
}