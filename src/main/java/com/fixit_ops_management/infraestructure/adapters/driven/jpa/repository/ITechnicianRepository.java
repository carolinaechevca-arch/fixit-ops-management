package com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository;

import com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ITechnicianRepository extends JpaRepository<TechnicianEntity, Long> {
    Optional<TechnicianEntity> findByDni(String dni);
}