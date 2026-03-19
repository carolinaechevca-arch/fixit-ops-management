package com.fixit_ops_management.domain.service;

import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import com.fixit_ops_management.domain.exceptions.*;
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

    public void validateTechnicianCanChangeCategory(Technician technician, TechnicianCategory newCategory) {
        if (technician.getStatus() != TechnicianStatus.AVAILABLE || technician.getTaskCount() > 0) {
            throw new TechnicianBusyException(
                    String.format(DomainConstants.TECHNICIAN_BUSY_MESSAGE,
                            technician.getName())
            );
        }
        if (technician.getCategory().equals(newCategory)) {
            throw new TechnicianSameCategoryException(
                    String.format(DomainConstants.TECHNICIAN_SAME_CATEGORY_MESSAGE,
                            technician.getId(), technician.getCategory())
            );
        }
    }

    public void validateTechnicianCanBeDeleted(Technician technician) {
        if (technician.getStatus() != TechnicianStatus.AVAILABLE  || technician.getTaskCount() > 0) {
            throw new TechnicianCannotBeDeletedException(
                    String.format(DomainConstants.TECHNICIAN_CANNOT_BE_DELETED_MESSAGE, technician.getId(), technician.getStatus()));
        }
    }
    public Technician releaseTechnicianLoad(Technician technician, int pointsToSubtract) {
        int newPoints = Math.max(0, technician.getCurrentPoints() - pointsToSubtract);
        int newTaskCount = Math.max(0, technician.getTaskCount() - 1);

        TechnicianStatus newStatus = (newPoints < technician.getCategory().getMaxPoints())
                ? TechnicianStatus.AVAILABLE
                : TechnicianStatus.BUSY;

        if (newTaskCount == 0) newStatus = TechnicianStatus.AVAILABLE;

        return technician.toBuilder()
                .currentPoints(newPoints)
                .taskCount(newTaskCount)
                .status(newStatus)
                .build();
    }

}