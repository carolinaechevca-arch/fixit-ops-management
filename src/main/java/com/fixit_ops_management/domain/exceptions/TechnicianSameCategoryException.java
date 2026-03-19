package com.fixit_ops_management.domain.exceptions;

public class TechnicianSameCategoryException extends RuntimeException{
    public TechnicianSameCategoryException(String message) {
        super(message);
    }
}
