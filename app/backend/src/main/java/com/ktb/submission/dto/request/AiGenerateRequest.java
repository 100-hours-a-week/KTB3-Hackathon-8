package com.ktb.submission.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiGenerateRequest {

    private Integer people;
    private String location;

    private List<String> preferences;
    private List<String> avoid;

    @JsonProperty("budget_per_person")
    private Integer budgetPerPerson;

    private List<RestaurantCandidate> candidates;

    @JsonProperty("max_new_tokens")
    private int maxNewTokens;
}
