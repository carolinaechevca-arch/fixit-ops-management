package com.fixit_ops_management.domain.model;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Technician {
    Long id;
    String dni;
    String name;
    TechnicianCategory category;
    TechnicianStatus status;
    Integer taskCount;
    Integer currentPoints;

    public static Technician createNew(String dni , String name, TechnicianCategory category) {
        return Technician.builder()
                .dni(dni)
                .name(name)
                .category(category)
                .status(TechnicianStatus.AVAILABLE)
                .taskCount(0)
                .currentPoints(0)
                .build();
    }
    public boolean canAcceptTaskPoints(int points) {
        if (this.category == TechnicianCategory.MASTER) return false;
        return (this.currentPoints + points) <= this.category.getMaxPoints();
    }

    //RF13 Y RF14
    public boolean canTakeTask(int points) {
        return (this.currentPoints + points) <= this.category.getMaxPoints();
    }

    public Technician assignPoints(int points) {
        return this.toBuilder()
                .currentPoints(this.currentPoints + points)
                .build();
    }

    private int getMaxCapacity() {
        return switch (this.category) {
            case JUNIOR -> 8;
            case SEMI_SENIOR -> 13;
            case SENIOR -> 21;
            case MASTER -> Integer.MAX_VALUE;
        };
    }
}
