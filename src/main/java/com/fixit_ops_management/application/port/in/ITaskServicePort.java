package com.fixit_ops_management.application.port.in;

import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.domain.model.Task;

import java.util.List;

public interface ITaskServicePort {

    Task createTask(Task task);

    List<Task> getAllTasks();

    Task getTaskById(Long id);

    void deleteTask(Long id);

    Task assignUrgentTask(Long taskId);

    AutoAssignSummary autoAssignAllUrgentTasks();
}
