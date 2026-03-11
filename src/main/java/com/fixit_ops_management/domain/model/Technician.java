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
}
