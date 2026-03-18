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
    public AutoAssignSummary autoAssignAllUrgentTasks() {
        List<Task> urgentPendingTasks = taskPersistencePort.findAll()
                .stream()
                .filter(Task::isUrgent)
                .filter(task -> TaskStatus.PENDING.equals(task.getStatus()))
                .toList();

        if (urgentPendingTasks.isEmpty()) {
            return AutoAssignSummary.builder()
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

        return AutoAssignSummary.builder()
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
        List<Technician> masters = technicianPersistencePort.findByCategory(TechnicianCategory.MASTER)
                .stream()
                .filter(m -> m.getStatus() != TechnicianStatus.NOT_AVAILABLE)
                .toList();

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

        Technician updatedMaster = assignmentStrategy.updateTechnicianState(selected.master(), 0);
        technicianPersistencePort.saveTechnician(updatedMaster);

        Task updated = task.toBuilder()
                .technicianId(selected.master().getId())
                .status(TaskStatus.ASSIGNED)
                .build();

        return taskPersistencePort.save(updated);
    }

    //Nuevos RF13 Y RF14
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
                    technicianPersistencePort.saveTechnician(tech);
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

        technicianPersistencePort.saveTechnician(technician);
        taskPersistencePort.save(task);
    }
    @Override
    public Task updateTask(Long id, Task updatedTask) {

        Task existingTask = getTaskById(id);

        TaskPriority oldPriority = existingTask.getPriority();
        TaskPriority newPriority = updatedTask.getPriority();

        Task taskToSave = existingTask.toBuilder()
                .name(updatedTask.getName())
                .description(updatedTask.getDescription())
                .priority(newPriority)
                .build();

        // Si la tarea no tiene técnico asignado
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

        // Buscar técnico actual
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

        // Si cambia a URGENT, siempre se quita el actual y se manda a MASTER
        if (newPriority == TaskPriority.URGENT) {

            Technician releasedTechnician = releaseTechnicianLoad(technician, oldPriority.getPoints());
            technicianPersistencePort.saveTechnician(releasedTechnician);

            Task urgentTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return assignTaskToMaster(urgentTask);
        }

        // Si el técnico actual es MASTER y la tarea deja de ser urgente,
        // ya no debe quedarse con la tarea
        if (technician.getCategory() == TechnicianCategory.MASTER) {
            Task unassignedTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return taskPersistencePort.save(unassignedTask);
        }

        // Recalcular puntos reales: restar la prioridad vieja y sumar la nueva
        int recalculatedPoints = technician.getCurrentPoints()
                - oldPriority.getPoints()
                + newPriority.getPoints();

        // Si ya no soporta la carga, quitar asignación y dejar en espera
        if (recalculatedPoints > technician.getCategory().getMaxPoints()) {

            Technician releasedTechnician = releaseTechnicianLoad(technician, oldPriority.getPoints());
            technicianPersistencePort.saveTechnician(releasedTechnician);

            Task unassignedTask = taskToSave.toBuilder()
                    .technicianId(null)
                    .status(TaskStatus.PENDING)
                    .build();

            return taskPersistencePort.save(unassignedTask);
        }

        // Si sí soporta la nueva carga, se actualiza el técnico y la tarea sigue asignada
        Technician adjustedTechnician = technician.toBuilder()
                .currentPoints(recalculatedPoints)
                .status(calculateTechnicianStatus(technician, recalculatedPoints))
                .build();

        technicianPersistencePort.saveTechnician(adjustedTechnician);

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