package com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter;

import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITaskEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskJpaAdapter implements ITaskPersistencePort {

    private final ITaskRepository taskRepository;
    private final ITaskEntityMapper taskEntityMapper;

    @Override
    public Task save(Task task) {
        return taskEntityMapper.toDomain(
                taskRepository.save(taskEntityMapper.toEntity(task))
        );
    }

}