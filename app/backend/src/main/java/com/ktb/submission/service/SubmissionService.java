package com.ktb.submission.service;

import com.ktb.group.domain.Group;
import com.ktb.group.exception.NonExistGroupException;
import com.ktb.group.repository.GroupRepository;
import com.ktb.group.service.GroupService;
import com.ktb.restaurant.google.dto.PlaceSummaryDto;
import com.ktb.restaurant.google.service.RestaurantSearchService;
import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.FinalResponseDto;
import com.ktb.submission.dto.TotalUserSubmission;
import com.ktb.submission.dto.request.AiGenerateRequest;
import com.ktb.submission.dto.request.RestaurantCandidate;
import com.ktb.submission.dto.request.SubmitRequest;
import com.ktb.submission.dto.response.AiGenerateResponse;
import com.ktb.submission.dto.response.AiRecommendation;
import com.ktb.submission.exception.AlreadySubmittedUserException;
import com.ktb.submission.repository.SubmissionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://3.236.242.98:8000")
            .build();

    private final SubmissionRepository submissionRepository;

    private final GroupRepository groupRepository;

    private final RestaurantSearchService restaurantSearchService;

    private final GroupService groupService;

    private final static String ALREADY_SUBMITTED = "이미 제출한 사용자입니다.";

    public void userSubmit(Long groupId, SubmitRequest submission) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        submissionRepository.findByNickname(submission.nickname())
                .ifPresent(existSubmission -> {
                    log.info(ALREADY_SUBMITTED);
                    throw new AlreadySubmittedUserException(ALREADY_SUBMITTED);
                });

        submissionRepository.save(submission.toEntity(group));

        groupService.submitMember(groupId, group.getOwner().getId(), submission.nickname());
    }

    public FinalResponseDto totalSubmit(Long groupId){
        List<Submission> Submissions =
                submissionRepository.findAllByGroupId(groupId);

        Group group = groupRepository.findById(groupId).orElseThrow(NonExistGroupException::new);

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


        int budgetPerPerson;
        int totalPeopleCnt = group.getMaxCapacity();
        int budget = group.getBudget();

        if (budget == 0) {
            budgetPerPerson = 0;
        } else {
            budgetPerPerson = budget / totalPeopleCnt;
        }

        // Google Places API로 후보 레스토랑 검색
        List<PlaceSummaryDto> placeSummaries = restaurantSearchService.findRestaurantsByStation(group.getStation());
        log.info("Found {} restaurant candidates for station: {}", placeSummaries.size(), group.getStation());

        // PlaceSummaryDto를 Python Restaurant 스키마로 변환
        List<RestaurantCandidate> candidates = placeSummaries.stream()
                .map(RestaurantCandidate::fromPlaceSummary)
                .toList();

        // AI 요청
        AiGenerateRequest request = new AiGenerateRequest(
                group.getMaxCapacity(),                       // people
                group.getStation(),                          // location (필수)
                total.getLikedFoodsList(),  // preferences
                total.getDisLikedFoodsList(), // avoid
                budgetPerPerson,                       // budget_per_person
                candidates,                              // candidates (Python Restaurant 스키마 형식)
                400                         // max_new_tokens
        );

        log.info("Sending request to LLM server: {}", request);

        AiGenerateResponse aiResponse = webClient.post()
                .uri("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error("LLM Server Error Response: {}", errorBody))
                                .map(errorBody -> new RuntimeException("LLM Server Error (" + response.statusCode() + "): " + errorBody))
                )
                .bodyToMono(AiGenerateResponse.class)
                .doOnSuccess(resp -> log.info("LLM response received: {}", resp))
                .doOnError(error -> log.error("LLM request failed", error))
                .block();

        AiGenerateResponse aiGenerateResponse = new AiGenerateResponse(aiResponse.getResults());

        return new FinalResponseDto(aiGenerateResponse, null);
    }
}
