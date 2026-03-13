package com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper;

import com.fixit_ops_management.domain.model.Task;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ITaskEntityMapper {

    /* Patrón Mapper: Aísla el modelo de datos de la base de datos
       del modelo de negocio inmutable. */

    TaskEntity toEntity(Task task);

    Task toDomain(TaskEntity taskEntity);

    List<Task> toDomainList(List<TaskEntity> taskEntities);
}