package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.exceptions.TechnicianAlreadyExistsException;
import com.fixit_ops_management.domain.exceptions.TechnicianBusyException;
import com.fixit_ops_management.domain.exceptions.TechnicianNotFoundException;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.util.constants.DomainConstants;

import java.util.Optional;

public class TechnicianDomainService {

    public void validateTechnicianDoesNotExistByDni(Optional<Technician> technicianOptional, String dni) {
        if (technicianOptional.isPresent()) {
            throw new TechnicianAlreadyExistsException(
                    String.format(DomainConstants.TECHNICIAN_DNI_ALREADY_EXISTS_MESSAGE, dni)
            );
        }
    }

    public Technician validateTechnicianExists(Optional<Technician> technicianOptional, Long id) {
        return technicianOptional.orElseThrow(() ->
                new TechnicianNotFoundException(
                        String.format(DomainConstants.TECHNICIAN_NOT_FOUND_MESSAGE, id)
                )
        );
    }

    public void validateTechnicianCanChangeCategory(Technician technician) {
        if (technician.getStatus() != TechnicianStatus.AVAILABLE || technician.getTaskCount() > 0) {
            throw new TechnicianBusyException(
                    String.format(DomainConstants.TECHNICIAN_BUSY_MESSAGE,
                            technician.getName())
            );
        }
    }
}