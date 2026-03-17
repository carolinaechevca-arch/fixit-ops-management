package com.fixit_ops_management.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Builder
@Getter
public class TechnicianWorkload {
    Technician technician;
    Integer availablePoints;
    List<Task> activeAssignments;

    public static TechnicianWorkload createNew(Technician tech, List<Task> tasks) {
        return TechnicianWorkload.builder()
                .technician(tech)
                .availablePoints(tech.getAvailablePoints())
                .activeAssignments(tasks)
                .build();
    }
}
