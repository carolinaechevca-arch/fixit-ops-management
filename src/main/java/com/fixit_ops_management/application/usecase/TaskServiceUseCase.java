package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.domain.model.MasterWithUrgentCount;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.service.AssignmentStrategy;
import com.fixit_ops_management.domain.service.TaskDomainService;
import com.fixit_ops_management.domain.service.TechnicianDomainService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TaskServiceUseCase implements ITaskServicePort {

    private final ITaskPersistencePort taskPersistencePort;
    private final ITechnicianPersistencePort technicianPersistencePort;
    private final TechnicianDomainService technicianDomainService;
    private final TaskDomainService taskDomainService;
    private final AssignmentStrategy assignmentStrategy;

    @Override
    public Task create(Task task) {
        Task newTask = Task.createNewPending(task.getName(), task.getDescription(), task.getPriority());
        return (newTask.isUrgent()) ? handleUrgentCreation(newTask) : handleStandardCreation(newTask);
    }

    private Task handleUrgentCreation(Task task) {
        List<Technician> availableMasters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER).stream()
                .filter(m -> m.getStatus() != TechnicianStatus.NOT_AVAILABLE)
                .toList();

        if (availableMasters.isEmpty()) {
            return taskPersistencePort.save(task);
        }

        return assignTaskToMaster(task);
    }

    private Task handleStandardCreation(Task task) {
        List<Technician> allTechs = technicianPersistencePort.findAll();
        return assignmentStrategy.findTechnicianByHierarchy(allTechs, task)
                .map(selected -> executeAssignment(task, selected, task.getPriority().getPoints()))
                .orElseGet(() -> taskPersistencePort.save(task));
    }

    @Override
    public List<Task> getAll() {
        return taskPersistencePort.findAll();
    }

    @Override
    public Task getById(Long id) {
        return taskDomainService.validateTaskExist(taskPersistencePort.findById(id), id);
    }

    @Override
    public void delete(Long id) {
        Task task = getById(id);
        taskDomainService.validateTaskCanBeDeleted(task);

        if (task.getTechnicianId() != null) {
            updateTechnicianOnTaskDeletion(task);
        }

        taskPersistencePort.deleteById(id);
    }

    private void updateTechnicianOnTaskDeletion(Task task) {
        technicianPersistencePort.findById(task.getTechnicianId())
                .ifPresent(technician -> {
                    Technician updatedTechnician = assignmentStrategy.releaseTechnicianLoad(
                            technician,
                            task.getPriority().getPoints()
                    );
                    technicianPersistencePort.save(updatedTechnician);
                });
    }

    @Override
    public AutoAssignSummary autoAssignAllUrgentTasks() {
        List<Task> pendingUrgent = taskDomainService.getPendingUrgentTasks(taskPersistencePort.findAll());

        if (pendingUrgent.isEmpty()) return AutoAssignSummary.buildEmptySummary();

        pendingUrgent.forEach(this::assignTaskToMaster);

        List<Task> remaining = taskDomainService.getPendingUrgentTasks(taskPersistencePort.findAll());
        long assignedCount = pendingUrgent.size() - remaining.size();

        return AutoAssignSummary.buildFinalSummary(assignedCount, remaining.size());
    }

    @Override
    public Task assignUrgentTask(Long taskId) {
        Task task = getById(taskId);
        taskDomainService.validateTaskUrgent(task);
        return assignTaskToMaster(task);
    }

    private Task assignTaskToMaster(Task task) {
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER).stream()
                .filter(m -> m.getStatus() != TechnicianStatus.NOT_AVAILABLE)
                .toList();

        if (masters.isEmpty()) {
            return taskPersistencePort.save(task);
        }

        List<MasterWithUrgentCount> mastersWithCount = masters.stream()
                .map(m -> new MasterWithUrgentCount(m, taskPersistencePort.countUrgentTasksByTechnicianId(m.getId())))
                .toList();

        Technician selected = taskDomainService.selectBestMaster(mastersWithCount);
        return executeAssignment(task, selected, 0);
    }

    private Task executeAssignment(Task task, Technician technician, int points) {
        Technician updatedTech = assignmentStrategy.updateTechnicianState(technician, points);
        technicianPersistencePort.save(updatedTech);

        Task assignedTask = task.toBuilder()
                .technicianId(technician.getId())
                .status(TaskStatus.ASSIGNED)
                .build();

        return taskPersistencePort.save(assignedTask);
    }

    @Override
    public void processWaitingTasks() {
        List<Task> waitingTasks = taskPersistencePort.findByStatus(TaskStatus.PENDING);

        if (waitingTasks == null || waitingTasks.isEmpty()) {
            return;
        }

        List<Technician> technicians = technicianPersistencePort.findAll();

        for (Task task : waitingTasks) {
            assignmentStrategy.findTechnicianByHierarchy(technicians, task)
                    .ifPresent(selected -> executeAssignment(task, selected, task.getPriority().getPoints()));
        }
    }

    @Override
    public void startTask(Long taskId) {
        Task task = getById(taskId);
        taskDomainService.validateStatusAssigned(task);

        Task startedTask = task.start();
        taskPersistencePort.save(startedTask);
    }

    @Override
    public void completeTask(Long taskId) {
        Task task = getById(taskId);
        taskDomainService.validateStatusProgress(task);

        Technician technician = technicianDomainService.validateTechnicianExists(
                technicianPersistencePort.findById(task.getTechnicianId()),
                task.getTechnicianId()
        );

        Technician updatedTechnician = assignmentStrategy.releaseTechnicianLoad(
                technician,
                task.getPriority().getPoints()
        );

        Task completedTask = task.complete();

        technicianPersistencePort.save(updatedTechnician);
        taskPersistencePort.save(completedTask);
    }

    @Override
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getById(id);
        taskDomainService.validatePriorityTask(existingTask, updatedTask);
        Task taskToSave = mapBasicInfo(existingTask, updatedTask);

        if (existingTask.getTechnicianId() == null) {
            return handleUnassignedTaskUpdate(taskToSave);
        }

        Technician technician = technicianPersistencePort.findById(existingTask.getTechnicianId())
                .orElse(null);

        if (technician == null) {
            return saveAsUnassigned(taskToSave);
        }

        return processTaskWithTechnician(taskToSave, existingTask, technician);
    }

    private Task processTaskWithTechnician(Task taskToSave, Task existingTask, Technician technician) {
        TaskPriority oldPriority = existingTask.getPriority();
        TaskPriority newPriority = taskToSave.getPriority();

        if (newPriority == TaskPriority.URGENT || technician.getCategory() == TechnicianCategory.MASTER) {
            releaseAndSaveTechnician(technician, oldPriority.getPoints());
            return assignTaskToMaster(prepareForUnassignment(taskToSave));
        }

        int newTotalPoints = assignmentStrategy.calculateRecalculatedPoints(technician, oldPriority.getPoints(), newPriority.getPoints());

        if (assignmentStrategy.isOverloaded(technician, newTotalPoints)) {
            releaseAndSaveTechnician(technician, oldPriority.getPoints());
            return saveAsUnassigned(taskToSave);
        }

        Technician updatedTech = assignmentStrategy.updateTechnicianPoints(technician, newTotalPoints);
        technicianPersistencePort.save(updatedTech);

        return taskPersistencePort.save(taskToSave);
    }

    private Task handleUnassignedTaskUpdate(Task task) {
        if (task.getPriority() == TaskPriority.URGENT) {
            return assignTaskToMaster(prepareForUnassignment(task));
        }
        return taskPersistencePort.save(task);
    }

    private void releaseAndSaveTechnician(Technician technician, int points) {
        Technician released = assignmentStrategy.releaseTechnicianLoad(technician, points);
        technicianPersistencePort.save(released);
    }

    private Task mapBasicInfo(Task existing, Task updated) {
        return existing.toBuilder()
                .name(updated.getName())
                .description(updated.getDescription())
                .priority(updated.getPriority())
                .build();
    }

    private Task prepareForUnassignment(Task task) {
        return task.toBuilder()
                .technicianId(null)
                .status(TaskStatus.PENDING)
                .build();
    }

    private Task saveAsUnassigned(Task task) {
        return taskPersistencePort.save(prepareForUnassignment(task));
    }
}