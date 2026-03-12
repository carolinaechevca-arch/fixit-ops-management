package com.fixit_ops_management.domain.service;


import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.util.constants.DomainConstants;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TaskDomainService {
    public Optional<Technician> findBestTechnicianForAutoAssignment(List<Technician> technicians, Task task) {
        return technicians.stream()
                .filter(tech -> tech.getStatus() == TechnicianStatus.AVAILABLE)
                .filter(tech -> canTechnicianHandleTask(tech, task))
                .sorted(Comparator
                        .comparing(Technician::getCategory)
                        .thenComparing(Comparator.comparing(Technician::getCurrentPoints).reversed()))
                .findFirst();
    }

    private boolean canTechnicianHandleTask(Technician tech, Task task) {
        int maxPoints = tech.getCategory().getMaxPoints();
        if (maxPoints == DomainConstants.MASTER_MAX_POINTS) return true;

        return (tech.getCurrentPoints() + task.getPriority().getPoints()) <= maxPoints;
    }
}

