package com.ktb.group.dto;

import com.ktb.group.domain.Group;

public record GroupFinalMeta(
        int maxCapacity,
        int budget
) {
    public static GroupFinalMeta from(Group group) {
        return new GroupFinalMeta(group.getMaxCapacity(), group.getBudget());
    }
}
