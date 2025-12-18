package com.ktb.submission.service;

import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.PromptRequestDto;
import com.ktb.submission.dto.PromptResponseDto;
import com.ktb.submission.dto.TotalUserSubmission;
import com.ktb.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    SubmissionRepository submissionRepository;

    public Submission userSubmit(Submission submission) {

        Optional<Submission> existingSubmission =
                submissionRepository.findByNickname(submission.getNickname());

        if (existingSubmission.isPresent()) {
            log.warn("이미 제출한 사용자입니다.");
            throw new IllegalStateException("이미 제출한 사용자입니다.");
        }

        return submissionRepository.save(submission);
    }

    public PromptResponseDto totalSubmit(Long groupId){
        List<Submission> Submissions =
                submissionRepository.findAllByGroupId(groupId);

        TotalUserSubmission total = new TotalUserSubmission();

        List<String> likeFoodList = Submissions.stream()
                .map(Submission::getLikedFoods).toList();
        List<String> disLikedFoodList = Submissions.stream()
                .map(Submission::getDisLikedFoods).toList();
        List<String> forbiddenFoodList = Submissions.stream()
                .map(Submission::getForbiddenFoods).toList();

        total.getLikedFoodsList().addAll(likeFoodList);
        total.getDisLikedFoodsList().addAll(disLikedFoodList);
        total.getForbiddenFoodsList().addAll(forbiddenFoodList);

        //1. LLM 요청
        PromptRequestDto promptRequest = new PromptRequestDto(null, total);

        //2. LLM 응답 -> 파싱&포매팅 후 return
        return null;
    }
}
