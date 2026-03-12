package com.fixit_ops_management.application.port.in;


import com.fixit_ops_management.domain.model.Task;

public interface ITaskServicePort {
    Task createTask(Task task);

}