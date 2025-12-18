package com.ktb.submission.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ktb.group.domain.Group;
import com.ktb.submission.domain.Submission;
import java.util.Date;
import java.util.List;

public record SubmitRequest(
        String nickname,
        String gender,
        Integer age,

        @JsonProperty("excluded_dates")
        List<Date> excludedDates,

        @JsonProperty("preferred_foods")
        String preferredFoods,

        @JsonProperty("avoided_foods")
        String avoidedFoods,

        @JsonProperty("excluded_foods")
        String excludedFoods
) {
        public Submission toEntity(Group group) {
                return Submission.create(
                        group,
                        nickname,
                        preferredFoods,
                        avoidedFoods,
                        excludedFoods,
                        excludedDates
                );
        }

        public SubmitRequest withNickname(String newNickname) {
                return new SubmitRequest(
                        newNickname,
                        this.gender,
                        this.age,
                        this.excludedDates,
                        this.preferredFoods,
                        this.avoidedFoods,
                        this.excludedFoods
                );
        }

}
