package com.fixit_ops_management.application.port.out;


import com.fixit_ops_management.domain.model.Task;

public interface ITaskPersistencePort {
    Task save(Task task);

}