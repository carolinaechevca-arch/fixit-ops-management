package com.fixit_ops_management.domain.exceptions;

public class NoMasterTechniciansAvailableException extends RuntimeException {
    public NoMasterTechniciansAvailableException(String message) {
        super(message);
    }
}
