package com.fixit_ops_management.domain.exceptions;

public class TechnicianCannotBeDeletedException extends RuntimeException {
    public TechnicianCannotBeDeletedException(String message) {
        super(message);
    }
}
