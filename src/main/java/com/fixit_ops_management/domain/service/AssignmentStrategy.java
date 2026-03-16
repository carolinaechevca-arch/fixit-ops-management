package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AssignmentStrategy {

    public Optional<Technician> findTechnicianByHierarchy(List<Technician> technicians, Task task) {
        List<TechnicianCategory> hierarchy = List.of(
                TechnicianCategory.JUNIOR,
                TechnicianCategory.SEMI_SENIOR,
                TechnicianCategory.SENIOR);

        for (TechnicianCategory category : hierarchy) {
            Optional<Technician> selected = findInCategory(technicians, category, task.getPriority().getPoints());
            if (selected.isPresent())
                return selected;
        }
        return Optional.empty();
    }

    private Optional<Technician> findInCategory(List<Technician> techs, TechnicianCategory category, int taskPoints) {
        return techs.stream()
                .filter(t -> t.getCategory() == category)
                .filter(t -> t.getStatus() != TechnicianStatus.NOT_AVAILABLE)
                .filter(t -> (t.getCurrentPoints() + taskPoints) <= t.getCategory().getMaxPoints())
                .sorted(Comparator.comparing(Technician::getStatus).reversed()
                        .thenComparing(Technician::getCurrentPoints, Comparator.reverseOrder()))
                .findFirst();
    }

    public Technician updateTechnicianState(Technician technician, int pointsToAdd) {
        int newPoints = technician.getCurrentPoints() + pointsToAdd;
        int newTaskCount = technician.getTaskCount() + 1;

        TechnicianStatus nextStatus;

        if (technician.getCategory() == TechnicianCategory.MASTER) {
            nextStatus = (newTaskCount >= 3)
                    ? TechnicianStatus.NOT_AVAILABLE
                    : TechnicianStatus.BUSY;
        } else {
            nextStatus = (newPoints >= technician.getCategory().getMaxPoints())
                    ? TechnicianStatus.NOT_AVAILABLE
                    : TechnicianStatus.BUSY;
        }

        return technician.toBuilder()
                .currentPoints(newPoints)
                .taskCount(newTaskCount)
                .status(nextStatus)
                .build();
    }

}