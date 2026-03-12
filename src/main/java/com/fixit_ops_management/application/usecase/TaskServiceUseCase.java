package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.domain.model.Task;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskServiceUseCase implements ITaskServicePort {

    private final ITaskPersistencePort taskPersistencePort;

    @Override
    public Task createTask(Task task) {
        Task newTask = Task.createNew(
                task.getName(),
                task.getDescription(),
                task.getPriority()
        );
        return taskPersistencePort.save(newTask);
    }

}