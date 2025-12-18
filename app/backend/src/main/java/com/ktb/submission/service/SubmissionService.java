package com.ktb.submission.service;

import com.ktb.group.domain.Group;
import com.ktb.group.repository.GroupRepository;
import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.PromptRequestDto;
import com.ktb.submission.dto.PromptResponseDto;
import com.ktb.submission.dto.TotalUserSubmission;
import com.ktb.submission.dto.request.SubmitRequest;
import com.ktb.submission.exception.AlreadySubmittedUserException;
import com.ktb.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final GroupRepository groupRepository;

    private final static String ALREADY_SUBMITTED = "이미 제출한 사용자입니다.";

    public void userSubmit(Long groupId, SubmitRequest submission) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        submissionRepository.findByNickname(submission.nickname())
                .ifPresent(existSubmission -> {
                    log.info(ALREADY_SUBMITTED);
                    throw new AlreadySubmittedUserException(ALREADY_SUBMITTED);
                });

        submissionRepository.save(submission.toEntity(group));
    }

    public PromptResponseDto totalSubmit(Long groupId){
        List<Submission> Submissions =
                submissionRepository.findAllByGroupId(groupId);

        TotalUserSubmission total = new TotalUserSubmission();

        List<String> likeFoodList = Submissions.stream()
                .map(Submission::getPreferredFoods).toList();
        List<String> disLikedFoodList = Submissions.stream()
                .map(Submission::getAvoidedFoods).toList();
        List<String> forbiddenFoodList = Submissions.stream()
                .map(Submission::getExcludedFoods).toList();

        total.getLikedFoodsList().addAll(likeFoodList);
        total.getDisLikedFoodsList().addAll(disLikedFoodList);
        total.getForbiddenFoodsList().addAll(forbiddenFoodList);

        //1. LLM 요청
        PromptRequestDto promptRequest = new PromptRequestDto(null, total);

        //2. LLM 응답 -> 파싱&포매팅 후 return
        return null;
    }
}
