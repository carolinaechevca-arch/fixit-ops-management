package com.fixit_ops_management.domain.exceptions;

public class TaskNotUrgentException extends RuntimeException {
    public TaskNotUrgentException(String message) {
        super(message);
    }
}
