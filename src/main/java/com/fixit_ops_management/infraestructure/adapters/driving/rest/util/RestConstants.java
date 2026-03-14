package com.fixit_ops_management.infraestructure.adapters.driving.rest.util;

public class RestConstants {

        private RestConstants() {
            throw new IllegalStateException("Utility class");
        }

        public static final String DNI_REQUIRED = "DNI is required";
        public static final String DNI_SIZE = "DNI must be at most 20 characters";
        public static final String NAME_REQUIRED = "Name is required";
        public static final String NAME_SIZE = "Name must be at most 100 characters";
        public static final String CATEGORY_REQUIRED = "Category is required";
        public static final String CATEGORY_VALID = "Category must be JUNIOR, SEMI_SENIOR, SENIOR, or MASTER";

        public static final String TASK_NAME_REQUIRED = "Task name is required";
        public static final String PRIORITY_REQUIRED = "Priority is required (LOW, MEDIUM, HIGH, URGENT)";
        public static final String PRIORITY_VALID = "Priority must be LOW, MEDIUM, HIGH, or URGENT";

        public static final String NO_MASTER_AVAILABLE = "No Master technicians available in the system.";
        public static final String TASK_NOT_FOUND = "Task not found with ID: %s";
        public static final String TECHNICIAN_NOT_FOUND = "Technician not found with ID: %s";
    }