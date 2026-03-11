package com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper;

import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity.TechnicianEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ITechnicianEntityMapper {

    TechnicianEntity toEntity(Technician technician);

    Technician toDomain(TechnicianEntity technicianEntity);
}