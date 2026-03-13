package com.fixit_ops_management.domain.exceptions;

public class TaskCannotBeDeletedException extends RuntimeException {
    public TaskCannotBeDeletedException(String message) {
        super(message);
    }
}
