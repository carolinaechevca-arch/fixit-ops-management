package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.exceptions.TaskCannotBeDeletedException;
import com.fixit_ops_management.domain.exceptions.TaskNotFoundException;
import com.fixit_ops_management.domain.exceptions.TaskNotUrgentException;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.util.constants.DomainConstants;

import java.util.Optional;

public class TaskDomainService {

    public void validateTaskCanBeDeleted(Task task) {
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus()) || TaskStatus.COMPLETED.equals(task.getStatus())) {
            throw new TaskCannotBeDeletedException(
                    String.format(DomainConstants.TASK_CANNOT_BE_DELETED_MESSAGE, task.getId(), task.getStatus()));
        }
    }
    public Task validateTaskExist(Optional<Task> task, Long id) {
        if (!task.isPresent()) {
            throw new TaskNotFoundException(String.format(DomainConstants.TASK_NOT_FOUND_MESSAGE, id));
        }
        return task.get();
    }
    public void validateTaskUrgent(Task task) {
        if (!task.isUrgent()) {
            throw new TaskNotUrgentException(DomainConstants.TASK_NOT_URGENT_MESSAGE);
        }

        if (task.getStatus().equals(TaskStatus.ASSIGNED)) {
            throw new TaskNotUrgentException(
                    DomainConstants.TASK_NOT_ASSIGNED_MESSAGE);
        }
    }


}
