package com.fixit_ops_management.domain.exceptions;

public class TaskAlreadyHasPriorityException extends RuntimeException{
    public TaskAlreadyHasPriorityException(String message, Long id) {
        super(message);
    }
}
