package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.model.AutoAssignSummary;
import com.fixit_ops_management.domain.model.MasterWithUrgentCount;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.service.AssignmentStrategy;
import com.fixit_ops_management.domain.service.TaskDomainService;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
}