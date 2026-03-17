package com.fixit_ops_management.application.port.out;

import com.fixit_ops_management.domain.model.Task;

import java.util.List;
import java.util.Optional;

public interface ITaskPersistencePort {

    List<Task> findAll();

    Optional<Task> findById(Long id);

    Task save(Task task);

    void deleteById(Long id);

    long countUrgentTasksByTechnicianId(Long technicianId);

    List<Task> findByTechnicianId(Long technicianId);
}