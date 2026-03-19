package com.fixit_ops_management.domain.exceptions;

public class TaskMustBeProgressToStartException extends  RuntimeException{
    public TaskMustBeProgressToStartException(String message) {
        super(message);
    }
}
