package com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository;

import com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITaskRepository extends JpaRepository<TaskEntity, Long> {
}