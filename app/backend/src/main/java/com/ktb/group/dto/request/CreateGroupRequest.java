package com.ktb.group.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Date;

@Schema(description = "그룹 생성 요청")
public record CreateGroupRequest(
        @Schema(description = "최대 인원", example = "4", requiredMode = RequiredMode.REQUIRED)
        @JsonProperty("max_capacity")
        Integer maxCapacity,

        @Schema(description = "만남 역/장소", example = "강남역")
        String station,

        @Schema(description = "총 예산 (원)", example = "15000")
        Integer budget,

        @Schema(description = "모임 날짜 지정 여부", example = "true")
        @JsonProperty("has_scheduled_Date")
        boolean hasScheduledDate,

        @Schema(description = "모임 날짜 지정 여부", example = "YYYY-MM-DD")
        @JsonProperty("start_date")
        Date startDate,

        @Schema(description = "모임 날짜 지정 여부", example = "YYYY-MM-DD")
        @JsonProperty("end_date")
        Date endDate

) {
}
