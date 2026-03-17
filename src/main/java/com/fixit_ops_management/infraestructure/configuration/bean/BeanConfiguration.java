package com.fixit_ops_management.infraestructure.configuration.bean;

import com.fixit_ops_management.application.port.in.ITaskServicePort;
import com.fixit_ops_management.application.port.in.ITechnicianServicePort;
import com.fixit_ops_management.application.port.out.ITaskPersistencePort;
import com.fixit_ops_management.application.port.out.ITechnicianPersistencePort;
import com.fixit_ops_management.application.usecase.TaskServiceUseCase;
import com.fixit_ops_management.application.usecase.TechnicianUseCase;
import com.fixit_ops_management.domain.service.AssignmentStrategy;
import com.fixit_ops_management.domain.service.TaskDomainService;
import com.fixit_ops_management.domain.service.TechnicianDomainService;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter.TaskJpaAdapter;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.adapter.TechnicianJpaAdapter;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITaskEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.mapper.ITechnicianEntityMapper;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITaskRepository;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository.ITechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    @Bean
    public ITechnicianPersistencePort technicianPersistencePort(
            ITechnicianRepository technicianRepository,
            ITechnicianEntityMapper technicianEntityMapper
    ) {
       return new TechnicianJpaAdapter(technicianRepository, technicianEntityMapper);
    }

    @Bean
    public ITaskPersistencePort taskPersistencePort(
            ITaskRepository taskRepository,
            ITaskEntityMapper taskEntityMapper
    ) {
        return new TaskJpaAdapter(taskRepository, taskEntityMapper);
    }
    @Bean
    public AssignmentStrategy assignmentStrategy() {
        return new AssignmentStrategy();
    }

    @Bean
    public TechnicianDomainService technicianDomainService() {
        return new TechnicianDomainService();
    }

    @Bean
    public TaskDomainService taskDomainService() {
        return new TaskDomainService();
    }


    @Bean
    public ITechnicianServicePort technicianServicePort(
            ITechnicianPersistencePort technicianPersistencePort,
            TechnicianDomainService technicianDomainService,
            ITaskPersistencePort taskPersistencePort
    ) {
        return new TechnicianUseCase(technicianPersistencePort, technicianDomainService, taskPersistencePort);
    }

    @Bean
    public ITaskServicePort taskServicePort(
            ITaskPersistencePort taskPersistencePort,
            ITechnicianPersistencePort technicianPersistencePort,
            TaskDomainService taskDomainService,
            AssignmentStrategy assignmentStrategy
    ) {
        return new TaskServiceUseCase(taskPersistencePort, technicianPersistencePort, taskDomainService, assignmentStrategy);
    }
}