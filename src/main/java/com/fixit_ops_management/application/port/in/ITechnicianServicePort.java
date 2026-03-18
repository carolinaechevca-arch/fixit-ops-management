package com.fixit_ops_management.application.port.in;


import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.model.TechnicianWorkload;

import java.util.List;

public interface ITechnicianServicePort {
    Technician createTechnician(Technician technician);
    Technician updateTechnicianCategory(Long id, TechnicianCategory newCategory);
    List<Technician> getAllTechnicians();
    Technician getTechnicianById(Long id);
    TechnicianWorkload getTechnicianWorkload(Long id);
}
