package com.ktb.submission.service;

import com.ktb.group.domain.Group;
import com.ktb.group.repository.GroupRepository;
import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.FinalResponseDto;
import com.ktb.submission.dto.TotalUserSubmission;
import com.ktb.submission.dto.request.AiGenerateRequest;
import com.ktb.submission.dto.request.SubmitRequest;
import com.ktb.submission.dto.response.AiGenerateResponse;
import com.ktb.submission.dto.response.AiRecommendation;
import com.ktb.submission.exception.AlreadySubmittedUserException;
import com.ktb.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final GroupRepository groupRepository;

    private final static String ALREADY_SUBMITTED = "이미 제출한 사용자입니다.";


    //임시 DI용
    @Autowired
    WebClient webClient;


    public void userSubmit(Long groupId, SubmitRequest submission) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        submissionRepository.findByNickname(submission.nickname())
                .ifPresent(existSubmission -> {
                    log.info(ALREADY_SUBMITTED);
                    throw new AlreadySubmittedUserException(ALREADY_SUBMITTED);
                });

        submissionRepository.save(submission.toEntity(group));
    }

    public FinalResponseDto totalSubmit(Long groupId){
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




        // AI 요청
        AiGenerateRequest request = new AiGenerateRequest(
                null,                       // people
                null,                       // location
                total.getLikedFoodsList(),  // preferences
                total.getDisLikedFoodsList(), // avoid
                null,                       // budget_per_person
                null,                       // candidates
                400                         // max_new_tokens
        );

        AiGenerateResponse aiResponse = webClient.post()
                .uri("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiGenerateResponse.class)
                .block();


        AiGenerateResponse aiGenerateResponse = new AiGenerateResponse(aiResponse.getResults());

        return new FinalResponseDto(aiGenerateResponse, null);



    }
}
