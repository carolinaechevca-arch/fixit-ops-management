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

        TechnicianStatus nextStatus = determineNextStatus(technician, newPoints, newTaskCount);

        return buildUpdatedTechnician(technician, newPoints, newTaskCount, nextStatus);
    }

    private TechnicianStatus determineNextStatus(Technician technician, int points, int taskCount) {
        if (technician.getCategory() == TechnicianCategory.MASTER) {
            return calculateMasterStatus(taskCount);
        }
        return calculateStandardStatus(technician.getCategory(), points);
    }

    private TechnicianStatus calculateMasterStatus(int taskCount) {
        return (taskCount >= 3) ? TechnicianStatus.NOT_AVAILABLE : TechnicianStatus.BUSY;
    }

    private TechnicianStatus calculateStandardStatus(TechnicianCategory category, int points) {
        return (points >= category.getMaxPoints()) ? TechnicianStatus.NOT_AVAILABLE : TechnicianStatus.BUSY;
    }

    private Technician buildUpdatedTechnician(Technician technician, int points, int tasks, TechnicianStatus status) {
        return technician.toBuilder()
                .currentPoints(points)
                .taskCount(tasks)
                .status(status)
                .build();
    }


    public int calculateRecalculatedPoints(Technician technician, int oldPoints, int newPoints) {
        return technician.getCurrentPoints() - oldPoints + newPoints;
    }

    public boolean isOverloaded(Technician technician, int recalculatedPoints) {
        return recalculatedPoints > technician.getCategory().getMaxPoints();
    }

    public Technician updateTechnicianPoints(Technician technician, int newPoints) {
        TechnicianStatus nextStatus = (newPoints >= technician.getCategory().getMaxPoints())
                ? TechnicianStatus.NOT_AVAILABLE
                : TechnicianStatus.BUSY;

        return technician.toBuilder()
                .currentPoints(newPoints)
                .status(nextStatus)
                .build();
    }

    public Technician releaseTechnicianLoad(Technician technician, int pointsToSubtract) {
        int actualPointsToSubtract = (technician.getCategory() == TechnicianCategory.MASTER) ? 0 : pointsToSubtract;

        int newPoints = Math.max(0, technician.getCurrentPoints() - actualPointsToSubtract);
        int newTaskCount = Math.max(0, technician.getTaskCount() - 1);

        TechnicianStatus nextStatus = determineStatusAfterRelease(technician, newPoints, newTaskCount);

        return technician.toBuilder()
                .currentPoints(newPoints)
                .taskCount(newTaskCount)
                .status(nextStatus)
                .build();
    }

    private TechnicianStatus determineStatusAfterRelease(Technician technician, int points, int taskCount) {
        if (taskCount == 0) {
            return TechnicianStatus.AVAILABLE;
        }

        if (technician.getCategory() == TechnicianCategory.MASTER) {
            return (taskCount >= 3) ? TechnicianStatus.NOT_AVAILABLE : TechnicianStatus.BUSY;
        }

        return (points >= technician.getCategory().getMaxPoints())
                ? TechnicianStatus.NOT_AVAILABLE
                : TechnicianStatus.BUSY;
    }
}