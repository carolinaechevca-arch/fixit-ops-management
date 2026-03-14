package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.dto.AutoAssignResult;
import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.exceptions.NoMasterTechniciansAvailableException;
import com.fixit_ops_management.domain.model.MasterWithUrgentCount;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.service.AssignmentStrategy;
import com.fixit_ops_management.domain.service.TaskDomainService;
import com.fixit_ops_management.domain.util.constants.DomainConstants;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@RequiredArgsConstructor
public class TaskServiceUseCase implements ITaskServicePort {

    private final ITaskPersistencePort taskPersistencePort;
    private final ITechnicianPersistencePort technicianPersistencePort;

    private final TaskDomainService taskDomainService;
    private final AssignmentStrategy assignmentStrategy;

    @Override
    public Task createTask(Task task) {
        Task newTask = Task.createNew(task.getName(), task.getDescription(), task.getPriority());

        if (newTask.getPriority() == TaskPriority.URGENT) {
            return handleUrgentCreation(newTask);
        }

        return handleStandardCreation(newTask);
    }

    private Task handleUrgentCreation(Task task) {
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER);

        if (masters.isEmpty()) {
            return taskPersistencePort.save(task);
        }

        return assignTaskToMaster(task);
    }

    private Task handleStandardCreation(Task task) {
        List<Technician> allTechnicians = technicianPersistencePort.findAll();
        Optional<Technician> assignedTech = assignmentStrategy.findTechnicianByHierarchy(allTechnicians, task);

        if (assignedTech.isPresent()) {
            Technician selected = assignedTech.get();
            updateTechnicianState(selected, task.getPriority().getPoints());

            return taskPersistencePort.save(task.toBuilder()
                    .technicianId(selected.getId())
                    .status(TaskStatus.ASSIGNED)
                    .build());
        }

        return taskPersistencePort.save(task);
    }

    private void updateTechnicianState(Technician technician, int pointsToAdd) {
        technicianPersistencePort.saveTechnician(assignmentStrategy.updateTechnicianState(technician, pointsToAdd));
    }

    @Override
    public List<Task> getAllTasks() {
        return taskPersistencePort.findAll();
    }

    @Override
    public Task getTaskById(Long id) {
        return taskDomainService.validateTaskExist(taskPersistencePort.findById(id), id);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskDomainService.validateTaskCanBeDeleted(task);
        taskPersistencePort.deleteById(id);
    }

    @Override
    public Task assignUrgentTask(Long taskId) {
        Task task = getTaskById(taskId);
        taskDomainService.validateTaskUrgent(task);

        return assignTaskToMaster(task);
    }

    @Override
    public AutoAssignResult autoAssignAllUrgentTasks() {
        List<Task> urgentPendingTasks = taskPersistencePort.findAll()
                .stream()
                .filter(Task::isUrgent)
                .filter(task -> TaskStatus.PENDING.equals(task.getStatus()))
                .toList();

        if (urgentPendingTasks.isEmpty()) {
            return AutoAssignResult.builder()
                    .assignedCount(0)
                    .remainingPendingCount(0)
                    .success(true)
                    .message(DomainConstants.NO_PENDING_URGENT_TASKS_MESSAGE)
                    .build();
        }

        long assignedCount = 0;
        for (Task task : urgentPendingTasks) {
            try {
                assignTaskToMaster(task);
                assignedCount++;
            } catch (Exception e) {
            }
        }

        long remainingPending = taskPersistencePort.findAll()
                .stream()
                .filter(Task::isUrgent)
                .filter(task -> TaskStatus.PENDING.equals(task.getStatus()))
                .count();

        return AutoAssignResult.builder()
                .assignedCount(assignedCount)
                .remainingPendingCount(remainingPending)
                .success(remainingPending == 0)
                .message(remainingPending == 0
                        ? DomainConstants.ALL_URGENT_TASKS_ASSIGNED_MESSAGE
                        : String.format(DomainConstants.AUTO_ASSIGN_URGENT_TASKS_MESSAGE, assignedCount,
                        remainingPending))
                .build();
    }

    private Task assignTaskToMaster(Task task) {
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER);
        if (masters.isEmpty()) {
            throw new NoMasterTechniciansAvailableException(
                    DomainConstants.NO_MASTER_TECHNICIANS_AVAILABLE_MESSAGE);
        }

        List<MasterWithUrgentCount> mastersWithCount = masters.stream()
                .map(master -> new MasterWithUrgentCount(
                        master,
                        taskPersistencePort.countUrgentTasksByTechnicianId(master.getId())))
                .toList();

        long minCount = mastersWithCount.stream()
                .mapToLong(MasterWithUrgentCount::urgentCount)
                .min()
                .orElse(0L);

        List<MasterWithUrgentCount> candidates = mastersWithCount.stream()
                .filter(m -> m.urgentCount() == minCount)
                .toList();

        MasterWithUrgentCount selected = candidates.get(
                ThreadLocalRandom.current().nextInt(candidates.size()));

        Task updated = task.toBuilder()
                .technicianId(selected.master().getId())
                .status(TaskStatus.ASSIGNED)
                .build();

        return taskPersistencePort.save(updated);
    }



}