package com.fixit_ops_management.application.port.in;


import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;

public interface ITechnicianServicePort extends ICrudServicePort<Technician, Long> {

    Technician updateTechnicianCategory(Long id, TechnicianCategory newCategory);
    Technician getTechnicianById(Long id);
    TechnicianWorkload getTechnicianWorkload(Long id);
}
