package com.fixit_ops_management.application.port.in;


import com.fixit_ops_management.domain.model.Technician;

public interface ITechnicianServicePort {
    Technician createTechnician(Technician technician);
}
