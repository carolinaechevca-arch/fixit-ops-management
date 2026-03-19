package com.fixit_ops_management.application.port.out;

import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.model.Task;

import java.util.List;

public interface ITaskPersistencePort extends ICrudPersistencePort<Task, Long> {
    long countUrgentTasksByTechnicianId(Long technicianId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByTechnicianId(Long technicianId);
}