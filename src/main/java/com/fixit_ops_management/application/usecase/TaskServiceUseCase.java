package com.fixit_ops_management.application.usecase;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.domain.enums.TaskStatus;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.service.TaskDomainService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

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
                task.getPriority()
        );

        List<Technician> allTechnicians = technicianPersistencePort.findAll();
        Optional<Technician> assignedTech = taskDomainService.findBestTechnicianForAutoAssignment(allTechnicians, newTask);

        if (assignedTech.isPresent()) {
            Technician selected = assignedTech.get();

            newTask = newTask.toBuilder()
                    .technicianId(selected.getId())
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            Technician updatedTech = selected.toBuilder()
                    .currentPoints(selected.getCurrentPoints() + newTask.getPriority().getPoints())
                    .taskCount(selected.getTaskCount() + 1)
                    .build();

            technicianPersistencePort.saveTechnician(updatedTech);
        }

        return taskPersistencePort.save(newTask);
    }
}