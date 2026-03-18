package com.fixit_ops_management.application.port.in;

import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.domain.model.Task;

public interface ITaskServicePort extends ICrudServicePort<Task, Long> {
    Task assignUrgentTask(Long taskId);
    AutoAssignSummary autoAssignAllUrgentTasks();
}
