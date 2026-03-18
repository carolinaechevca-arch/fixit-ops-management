package com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper;

import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TechnicianRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianResponse;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianWorkloadResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ITechnicianRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "taskCount", ignore = true)
    @Mapping(target = "currentPoints", ignore = true)
    Technician toDomain(TechnicianRequest request);

    @Mapping(target = "status", source = "technician.status")
    @Mapping(target = "availablePoints", source = "availablePoints")
    @Mapping(target = "assignedTasks", source = "activeAssignments")
    TechnicianWorkloadResponse toWorkloadResponse(TechnicianWorkload workload);

    TechnicianResponse toResponse(Technician technician);


}