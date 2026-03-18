package com.fixit_ops_management.infraestructure.adapters.driven.jpa.repository;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ITechnicianRepository extends JpaRepository<TechnicianEntity, Long> {
    Optional<TechnicianEntity> findByDni(String dni);

    List<TechnicianEntity> findByCategory(TechnicianCategory category);

    //RF13 Y RF14
    @Query("SELECT t FROM TechnicianEntity t ORDER BY " +
            "CASE t.category " +
            "WHEN 'Junior' THEN 1 " +
            "WHEN 'SemiSenior' THEN 2 " +
            "WHEN 'Senior' THEN 3 END")
    List<TechnicianEntity> findAllOrderedByHierarchy();
}