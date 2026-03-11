package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.exceptions.TechnicianAlreadyExistsException;
import com.fixit_ops_management.domain.model.Technician;
import com.fixit_ops_management.domain.util.constants.DomainConstants;

import java.util.Optional;

public class TechnicianDomainService {

    public void validateTechnicianDoesNotExist(Optional<Technician> technicianOptional, String dni) {
        if (technicianOptional.isPresent()) {
            throw new TechnicianAlreadyExistsException(
                    String.format(DomainConstants.TECHNICIAN_ALREADY_EXISTS_MESSAGE, dni)
            );
        }
    }
}