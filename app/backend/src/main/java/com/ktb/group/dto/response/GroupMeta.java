package com.ktb.group.dto.response;

import java.util.Date;

public record GroupMeta(
        boolean isOwner,
        boolean hasScheduledDate,
        Date startDate,
        Date endDate
) {
}
