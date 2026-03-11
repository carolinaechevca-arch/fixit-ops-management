package com.fixit_ops_management.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechnicianCategory {
    JUNIOR(8),
    SEMI_SENIOR(13),
    SENIOR(21),
    MASTER(0);

    private final int maxPoints;
}