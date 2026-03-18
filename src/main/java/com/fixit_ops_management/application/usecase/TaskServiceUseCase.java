package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.exceptions.NoMasterTechniciansAvailableException;
import com.fixit_ops_management.domain.model.AutoAssignSummary;
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
    public Task create(Task task) {
        Task newTask = Task.createNew(task.getName(), task.getDescription(), task.getPriority());
        return (newTask.isUrgent()) ? handleUrgentCreation(newTask) : handleStandardCreation(newTask);
    }

    private Task handleUrgentCreation(Task task) {
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER);
        if (masters.isEmpty()) return taskPersistencePort.save(task);
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
        taskPersistencePort.deleteById(id);
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

        var waitingTasks = taskPersistencePort.findByStatus(TaskStatus.PENDING);

        if (waitingTasks == null || waitingTasks.isEmpty()) {
            return;
        }

        for (var task : waitingTasks) {

            var technicians = technicianPersistencePort.findAll();

            var ordered = technicians.stream()
                    .sorted((t1, t2) -> Integer.compare(getPriorityOrder(t1), getPriorityOrder(t2)))
                    .toList();
            for (var tech : ordered) {
                if (tech.canTakeTask(task.getPoints())) {

                    task = task.assignTo(tech.getId());
                    tech = tech.assignPoints(task.getPoints());

                    taskPersistencePort.save(task);
                    technicianPersistencePort.save(tech);
                    break;
                }
            }
        }
    }

    private int getPriorityOrder(Technician tech) {
        return switch (tech.getCategory()) {
            case JUNIOR -> 1;
            case SEMI_SENIOR -> 2;
            case SENIOR -> 3;
            case MASTER -> 4;
        };
    }

    @Override
    public void startTask(Long taskId) {
        var task = taskPersistencePort.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getStatus().equals(TaskStatus.ASSIGNED)) {
            throw new RuntimeException("Task must be ASSIGNED to start");
        }

        task = task.start();
        taskPersistencePort.save(task);

        taskPersistencePort.save(task);
    }

    @Override
    public void completeTask(Long taskId) {
        var task = taskPersistencePort.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getStatus().equals(TaskStatus.IN_PROGRESS)) {
            throw new RuntimeException("Task must be IN_PROGRESS to complete");
        }

        var technician = technicianPersistencePort.findById(task.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("Technician not found"));


        technician = technician.assignPoints(-task.getPoints());

        task = task.complete();

        technicianPersistencePort.save(technician);
        taskPersistencePort.save(task);
    }
    @Override
    public Task updateTask(Long id, Task updatedTask) {

        Task existingTask = getById(id);

        TaskPriority oldPriority = existingTask.getPriority();
        TaskPriority newPriority = updatedTask.getPriority();

        Task taskToSave = existingTask.toBuilder()
                .name(updatedTask.getName())
                .description(updatedTask.getDescription())
                .priority(newPriority)
                .build();

        if (existingTask.getTechnicianId() == null) {
            if (newPriority == TaskPriority.URGENT) {
                return assignTaskToMaster(
                        taskToSave.toBuilder()
                                .status(TaskStatus.PENDING)
                                .technicianId(null)
                                .build()
                );
            }
            return taskPersistencePort.save(taskToSave);
        }

        Technician technician = technicianPersistencePort.findById(existingTask.getTechnicianId())
                .orElse(null);

        if (technician == null) {
            return taskPersistencePort.save(
                    taskToSave.toBuilder()
                            .technicianId(null)
                            .status(TaskStatus.PENDING)
                            .build()
            );
        }

        if (newPriority == TaskPriority.URGENT) {

            Technician releasedTechnician = releaseTechnicianLoad(technician, oldPriority.getPoints());
            technicianPersistencePort.save(releasedTechnician);

            Task urgentTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return assignTaskToMaster(urgentTask);
        }

        if (technician.getCategory() == TechnicianCategory.MASTER) {
            Task unassignedTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return taskPersistencePort.save(unassignedTask);
        }

        int recalculatedPoints = technician.getCurrentPoints()
                - oldPriority.getPoints()
                + newPriority.getPoints();

        if (recalculatedPoints > technician.getCategory().getMaxPoints()) {

            Technician releasedTechnician = releaseTechnicianLoad(technician, oldPriority.getPoints());
            technicianPersistencePort.save(releasedTechnician);

            Task unassignedTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return taskPersistencePort.save(unassignedTask);
        }

        Technician adjustedTechnician = technician.toBuilder()
                .currentPoints(recalculatedPoints)
                .status(calculateTechnicianStatus(technician, recalculatedPoints))
                .build();

        technicianPersistencePort.save(adjustedTechnician);

        return taskPersistencePort.save(taskToSave);


    }

    private Technician releaseTechnicianLoad(Technician technician, int pointsToRemove) {
        int newPoints = Math.max(technician.getCurrentPoints() - pointsToRemove, 0);
        int newTaskCount = Math.max(technician.getTaskCount() - 1, 0);

        return technician.toBuilder()
                .currentPoints(newPoints)
                .taskCount(newTaskCount)
                .status(calculateTechnicianStatus(technician, newPoints))
                .build();
    }

    private TechnicianStatus calculateTechnicianStatus(Technician technician, int points) {
        if (points == 0) {
            return TechnicianStatus.AVAILABLE;
        }

        if (points >= technician.getCategory().getMaxPoints()) {
            return TechnicianStatus.NOT_AVAILABLE;
        }

        return TechnicianStatus.BUSY;
    }

}