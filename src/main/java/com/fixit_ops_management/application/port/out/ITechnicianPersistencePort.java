package com.fixit_ops_management.application.port.out;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;

import java.util.List;
import java.util.Optional;

public interface ITechnicianPersistencePort {
    Technician saveTechnician(Technician technician);
    Optional<Technician> findByDni(String dni);
    List<Technician> findAllTechnicians();
    Optional<Technician> findById(Long id);


    List<Technician> findAll();

    List<Technician> findByCategory(TechnicianCategory category);
}