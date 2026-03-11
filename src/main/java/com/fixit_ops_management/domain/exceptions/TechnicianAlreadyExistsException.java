package com.fixit_ops_management.domain.exceptions;

public class TechnicianAlreadyExistsException extends RuntimeException {
    public TechnicianAlreadyExistsException(String message) {
        super(message);
    }
}