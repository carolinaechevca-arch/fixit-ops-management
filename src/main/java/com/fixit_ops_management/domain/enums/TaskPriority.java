package com.fixit_ops_management.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskPriority {
    LOW(3),
    MEDIUM(5),
    HIGH(8),
    URGENT(0);
    private final int points;
}