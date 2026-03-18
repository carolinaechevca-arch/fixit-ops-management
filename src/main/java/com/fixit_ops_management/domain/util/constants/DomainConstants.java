package com.fixit_ops_management.domain.util.constants;

public final class DomainConstants {
    private DomainConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String TECHNICIAN_BUSY_MESSAGE = "Technician with ID %s is currently busy and cannot be assigned new tasks.";
    public static final String TECHNICIAN_DNI_ALREADY_EXISTS_MESSAGE = "Technician with DNI %s already exists.";
    public static final String TECHNICIAN_NOT_FOUND_MESSAGE = "Technician with ID %s already exists.";
    public static final String TASK_NOT_FOUND_MESSAGE = "Task with id %d was not found.";
    public static final String TASK_CANNOT_BE_DELETED_MESSAGE = "Task with id %d cannot be deleted because its status is %s.";
    public static final String TASK_NOT_URGENT_MESSAGE = "Task must have URGENT priority to be assigned to Master technicians.";
    public static final String TASK_NOT_ASSIGNED_MESSAGE = "Only tasks with ASSIGNED status can be reassigned to Master technicians.";
    public static final String NO_PENDING_URGENT_TASKS_MESSAGE = "No pending urgent tasks to assign";
    public static final String ALL_URGENT_TASKS_ASSIGNED_MESSAGE = "All urgent tasks assigned successfully";
    public static final String AUTO_ASSIGN_URGENT_TASKS_MESSAGE = "Assigned %d tasks. %d pending urgent tasks remain";
    public static final String NO_MASTER_TECHNICIANS_AVAILABLE_MESSAGE = "No Master technicians available to assign urgent tasks.";
}