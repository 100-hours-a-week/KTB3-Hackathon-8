package com.ktb.group.dto;

import com.ktb.group.domain.Group;

public record TempAggregation(
        int submitCnt,
        int totalCnt
) {
    public static TempAggregation from(Group group) {
        return new TempAggregation(group.getSubmitMembers().size(), group.getMaxCapacity());
    }
}
