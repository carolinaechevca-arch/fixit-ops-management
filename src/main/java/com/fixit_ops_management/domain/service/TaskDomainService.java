package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.exceptions.NoMasterTechniciansAvailableException;
import com.fixit_ops_management.domain.exceptions.TaskCannotBeDeletedException;
import com.fixit_ops_management.domain.exceptions.TaskNotFoundException;
import com.fixit_ops_management.domain.exceptions.TaskNotUrgentException;
import com.fixit_ops_management.domain.model.MasterWithUrgentCount;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.util.constants.DomainConstants;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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
    public List<Task> getPendingUrgentTasks( List<Task> tasks) {
        return tasks.stream()
                .filter(Task::isUrgent)
                .filter(task -> TaskStatus.PENDING.equals(task.getStatus()))
                .toList();
    }


    public Technician selectBestMaster(List<MasterWithUrgentCount> mastersWithCount) {
        if (mastersWithCount.isEmpty()) {
            throw new NoMasterTechniciansAvailableException(DomainConstants.NO_MASTER_TECHNICIANS_AVAILABLE_MESSAGE);
        }

        long minCount = mastersWithCount.stream()
                .mapToLong(MasterWithUrgentCount::urgentCount)
                .min()
                .orElse(0L);

        List<Technician> candidates = mastersWithCount.stream()
                .filter(m -> m.urgentCount() == minCount)
                .map(MasterWithUrgentCount::master)
                .toList();

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

}
