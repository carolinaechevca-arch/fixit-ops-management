package com.fixit_ops_management.domain.model;

import com.fixit_ops_management.domain.enums.TaskPriority;
import com.fixit_ops_management.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Task {
    Long id;
    String name;
    String description;
    TaskPriority priority;
    TaskStatus status;
    Long technicianId;
    LocalDateTime createdAt;
    LocalDateTime closedAt;

    public static Task createNew(String name, String description, TaskPriority priority) {
        return Task.builder()
                .name(name)
                .description(description)
                .priority(priority)
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isUrgent() {
        return TaskPriority.URGENT.equals(priority);
    }

    public Task assignTo(Long technicianId) {
        return this.toBuilder()
                .technicianId(technicianId)
                .status(TaskStatus.ASSIGNED)
                .build();
    }

    public Task start() {
        return this.toBuilder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    public Task complete() {
        return this.toBuilder()
                .status(TaskStatus.COMPLETED)
                .closedAt(LocalDateTime.now())
                .build();
    }

    public int getPoints() {
        return switch (priority) {
            case HIGH -> 8;
            case MEDIUM -> 5;
            case LOW -> 3;
            case URGENT -> 10;
        };
    }
}