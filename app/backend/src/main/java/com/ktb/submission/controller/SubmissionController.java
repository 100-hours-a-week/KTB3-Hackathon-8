package com.ktb.submission.controller;

import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.PromptResponseDto;
import com.ktb.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/submission")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    //맴버 개별 제출
    @PostMapping("/user")
    public ResponseEntity<Void> userSubmit(@RequestBody Submission submission) {

        submissionService.userSubmit(submission);

        return ResponseEntity.ok().build();
    }

    //총무 통합 제출
    @PostMapping("/total/{groupId}")
    public ResponseEntity<PromptResponseDto> totalSubmit(@PathVariable Long groupId) {

        PromptResponseDto promptResponse = submissionService.totalSubmit(groupId);

        return ResponseEntity.ok().body(promptResponse);
    }
}
