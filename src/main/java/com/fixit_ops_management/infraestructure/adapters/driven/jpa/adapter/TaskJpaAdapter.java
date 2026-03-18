package com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter;

import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITaskEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITaskRepository;
import lombok.RequiredArgsConstructor;
import com.fixit_ops_management.domain.enums.TaskStatus;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TaskJpaAdapter implements ITaskPersistencePort {

    private final ITaskRepository taskRepository;
    private final ITaskEntityMapper taskEntityMapper;

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(taskEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id)
                .map(taskEntityMapper::toDomain);
    }

    @Override
    public List<Task> findByTechnicianId(Long technicianId) {
        return taskEntityMapper.toDomainList(
                taskRepository.findByTechnicianId(technicianId)
        );
    }

    @Override
    public Task save(Task task) {
        return taskEntityMapper.toDomain(
                taskRepository.save(taskEntityMapper.toEntity(task)));
    }

    @Override
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public long countUrgentTasksByTechnicianId(Long technicianId) {
        return taskRepository.countByTechnicianIdAndPriority(technicianId, TaskPriority.URGENT);
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status)
                .stream()
                .map(taskEntityMapper::toDomain)
                .toList();
    }

}