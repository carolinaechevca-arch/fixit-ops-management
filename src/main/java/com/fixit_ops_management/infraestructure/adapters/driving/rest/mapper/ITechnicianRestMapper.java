package com.fixit_ops_management.infraestructure.adapters.driving.rest.mapper;

import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.request.TechnicianRequest;
import com.fixit_ops_management.infraestructure.adapters.driving.rest.dto.response.TechnicianResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ITechnicianRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "taskCount", ignore = true)
    @Mapping(target = "currentPoints", ignore = true)
    Technician toDomain(TechnicianRequest request);

    TechnicianResponse toResponse(Technician technician);


}