package com.ktb.submission.dto.request;

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

    private Integer budget_per_person;

    private Object candidates;

    private int max_new_tokens;
}
