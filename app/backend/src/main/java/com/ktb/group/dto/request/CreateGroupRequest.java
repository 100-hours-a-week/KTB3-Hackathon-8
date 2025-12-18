package com.ktb.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Schema(description = "그룹 생성 요청")
public record CreateGroupRequest(
        @Schema(description = "그룹 오너 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
        Long ownerId,

        @Schema(description = "최대 인원", example = "4", requiredMode = RequiredMode.REQUIRED)
        Integer maxCapacity,

        @Schema(description = "만남 역/장소", example = "강남역")
        String station,

        @Schema(description = "총 예산 (원)", example = "15000")
        Integer budget,

        @Schema(description = "모임 날짜 지정 여부", example = "true")
        boolean hasScheduledDate
) {
}
