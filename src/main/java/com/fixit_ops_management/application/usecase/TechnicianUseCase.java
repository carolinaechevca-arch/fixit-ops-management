package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;
import com.fixit_ops_management.domain.service.TechnicianDomainService;
import lombok.RequiredArgsConstructor;
import  java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TechnicianUseCase implements ITechnicianServicePort {

    private final ITechnicianPersistencePort technicianPersistencePort;
    private final TechnicianDomainService technicianDomainService;
    private final ITaskPersistencePort taskPersistencePort;

    @Override
    public Technician createTechnician(Technician technician) {
        Optional<Technician> existingTechnician = technicianPersistencePort.findByDni(technician.getDni());
        technicianDomainService.validateTechnicianDoesNotExistByDni(existingTechnician, technician.getDni());

        Technician newTechnician = Technician.createNew(
                technician.getDni(),
                technician.getName(),
                technician.getCategory()
        );

        return technicianPersistencePort.saveTechnician(newTechnician);
    }

    @Override
    public List<Technician> getAllTechnicians() {
            return technicianPersistencePort.findAllTechnicians();
    }

    @Override
    public Technician getTechnicianById(Long id) {
        return technicianPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
    }

    @Override
    public TechnicianWorkload getTechnicianWorkload(Long id){
        Technician technician = technicianDomainService.validateTechnicianExists(technicianPersistencePort.findById(id), id);

        List<Task> tasks = taskPersistencePort.findByTechnicianId(id);

        return TechnicianWorkload.createNew(technician, tasks);
    }
}