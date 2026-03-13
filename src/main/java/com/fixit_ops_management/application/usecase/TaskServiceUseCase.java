package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.dto.AutoAssignResult;
import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.exceptions.TaskNotFoundException;
import com.fixit_ops_management.domain.exceptions.NoMasterTechniciansAvailableException;
import com.fixit_ops_management.domain.exceptions.TaskNotUrgentException;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
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

    @Override
    public Task createTask(Task task) {
        Task newTask = Task.createNew(
                task.getName(),
                task.getDescription(),
                task.getPriority());

        List<Technician> allTechnicians = technicianPersistencePort.findAll();
        Optional<Technician> assignedTech = taskDomainService.findBestTechnicianForAutoAssignment(allTechnicians,
                newTask);

        if (assignedTech.isPresent()) {
            Technician selected = assignedTech.get();

            newTask = newTask.toBuilder()
                    .technicianId(selected.getId())
                    .status(TaskStatus.ASSIGNED)
                    .build();

            Technician updatedTech = selected.toBuilder()
                    .currentPoints(selected.getCurrentPoints() + newTask.getPriority().getPoints())
                    .taskCount(selected.getTaskCount() + 1)
                    .build();

            technicianPersistencePort.saveTechnician(updatedTech);
        }

        return taskPersistencePort.save(newTask);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskPersistencePort.findAll();
    }

    @Override
    public Task getTaskById(Long id) {
        return taskPersistencePort.findById(id)
                .orElseThrow(
                        () -> new TaskNotFoundException(String.format(DomainConstants.TASK_NOT_FOUND_MESSAGE, id)));
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

        if (!task.isUrgent()) {
            throw new TaskNotUrgentException(
                    "Task must have URGENT priority to be assigned to Master technicians.");
        }

        return assignTaskToMaster(task);
    }

    @Override
    public AutoAssignResult autoAssignAllUrgentTasks() {
        // Get all pending and urgent tasks
        List<Task> urgentPendingTasks = taskPersistencePort.findAll()
                .stream()
                .filter(Task::isUrgent)
                .filter(task -> TaskStatus.PENDING.equals(task.getStatus()))
                .toList();

        // Assign each pending urgent task
        long assignedCount = 0;
        for (Task task : urgentPendingTasks) {
            try {
                assignTaskToMaster(task);
                assignedCount++;
            } catch (Exception e) {
                // Continue with the next task if this one fails
            }
        }

        // Count remaining pending urgent tasks
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
                        ? "All urgent tasks assigned successfully"
                        : "Assigned " + assignedCount + " tasks. " + remainingPending + " pending urgent tasks remain")
                .build();
    }

    private Task assignTaskToMaster(Task task) {
        // Find all Master technicians
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER);
        if (masters.isEmpty()) {
            throw new NoMasterTechniciansAvailableException(
                    "No Master technicians available to assign urgent tasks.");
        }

        // Count urgent tasks assigned to each Master
        List<MasterWithUrgentCount> mastersWithCount = masters.stream()
                .map(master -> new MasterWithUrgentCount(
                        master,
                        taskPersistencePort.countUrgentTasksByTechnicianId(master.getId())))
                .toList();

        // Find the minimum number of urgent tasks assigned
        long minCount = mastersWithCount.stream()
                .mapToLong(MasterWithUrgentCount::urgentCount)
                .min()
                .orElse(0L);

        // Filter only Masters with the minimum count
        List<MasterWithUrgentCount> candidates = mastersWithCount.stream()
                .filter(m -> m.urgentCount == minCount)
                .toList();

        // If there's a tie, select one randomly
        MasterWithUrgentCount selected = candidates.get(
                ThreadLocalRandom.current().nextInt(candidates.size()));

        // Assign the task to the selected Master and mark it as ASSIGNED
        Task updated = task.toBuilder()
                .technicianId(selected.master().getId())
                .status(TaskStatus.ASSIGNED)
                .build();

        return taskPersistencePort.save(updated);
    }

    private record MasterWithUrgentCount(Technician master, long urgentCount) {
    }
}