package com.fixit_ops_management.domain.exceptions;

public class TechnicianBusyException extends RuntimeException{
    public TechnicianBusyException(String message) {
        super(message);
    }
}
