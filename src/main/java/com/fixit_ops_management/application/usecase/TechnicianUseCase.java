package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;
import com.fixit_ops_management.domain.service.TechnicianDomainService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TechnicianUseCase implements ITechnicianServicePort {

    private final ITechnicianPersistencePort technicianPersistencePort;
    private final TechnicianDomainService technicianDomainService;
    private final ITaskPersistencePort taskPersistencePort;

    @Override
    public Technician create(Technician technician) {
        Optional<Technician> existingTechnician = technicianPersistencePort.findByDni(technician.getDni());
        technicianDomainService.validateTechnicianDoesNotExistByDni(existingTechnician, technician.getDni());

        Technician newTechnician = Technician.createNew(
                technician.getDni(),
                technician.getName(),
                technician.getCategory()
        );

        return technicianPersistencePort.save(newTechnician);
    }

    @Override
    public List<Technician> getAll() {
            return technicianPersistencePort.findAll();
    }

    @Override
    public Technician getById(Long id) {
        return technicianDomainService.validateTechnicianExists(technicianPersistencePort.findById(id), id);
    }

    @Override
    public void delete(Long id) {
        technicianPersistencePort.deleteById(getById(id).getId());
    }

    @Override
    public TechnicianWorkload getTechnicianWorkload(Long id){
        Technician technician = technicianDomainService.validateTechnicianExists(technicianPersistencePort.findById(id), id);

        List<Task> tasks = taskPersistencePort.findByTechnicianId(id);

        return TechnicianWorkload.createNew(technician, tasks);
    }

    @Override
    public Technician updateTechnicianCategory(Long id, TechnicianCategory newTechnicianCategory){
        Optional<Technician> technician = technicianPersistencePort.findById(id);
        technicianDomainService.validateTechnicianExists(technician, id);
        technicianDomainService.validateTechnicianCanChangeCategory(technician.get());

        Technician updatedTechnicianCategory = technician.get()
                                                         .toBuilder()
                                                         .category(newTechnicianCategory)
                                                         .build();

        return technicianPersistencePort.save(updatedTechnicianCategory);
    }

    @Override
    public void deleteTechnician(Long id) {
        Technician technician = getTechnicianById(id);
        technicianDomainService.validateTechnicianCanBeDeleted(technician);
        technicianPersistencePort.deleteById(id);
    }
}