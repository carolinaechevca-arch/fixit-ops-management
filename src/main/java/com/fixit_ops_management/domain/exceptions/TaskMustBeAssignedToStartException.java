package com.fixit_ops_management.domain.exceptions;

public class TaskMustBeAssignedToStartException extends RuntimeException{
    public TaskMustBeAssignedToStartException(String message) {
        super(message);
    }
}
