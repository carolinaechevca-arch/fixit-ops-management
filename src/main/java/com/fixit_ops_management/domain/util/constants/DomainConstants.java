package com.fixit_ops_management.domain.util.constants;

public final class DomainConstants {
    private DomainConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int INITIAL_VALUE = 0;
    public static final String TECHNICIAN_ALREADY_EXISTS_MESSAGE = "Technician with DNI %s already exists.";
    public static final int MASTER_MAX_POINTS = 0; // 0 significa ilimitado
    public static final String TASK_NOT_FOUND_MESSAGE = "Task with id %d was not found.";
    public static final String TASK_CANNOT_BE_DELETED_MESSAGE = "Task with id %d cannot be deleted because its status is %s.";
}