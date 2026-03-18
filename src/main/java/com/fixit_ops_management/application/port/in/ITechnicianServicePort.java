package com.fixit_ops_management.application.port.in;


import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;

public interface ITechnicianServicePort extends ICrudServicePort<Technician, Long> {
    TechnicianWorkload getTechnicianWorkload(Long id);
    Technician updateCategory(Long id, TechnicianCategory newCategory);
}