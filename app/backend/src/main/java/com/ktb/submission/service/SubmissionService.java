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
import com.ktb.submission.exception.AlreadySubmittedUserException;
import com.ktb.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://3.236.242.98:8000")
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> {
                        ObjectMapper mapper = new ObjectMapper();
                        configurer.defaultCodecs().jackson2JsonEncoder(
                                new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                        configurer.defaultCodecs().jackson2JsonDecoder(
                                new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                    })
                    .build())
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
        log.info("=== [Service] Starting totalSubmit for groupId: {} ===", groupId);

        List<Submission> Submissions =
                submissionRepository.findAllByGroupId(groupId);
        log.info("[Service] Found {} submissions for groupId: {}", Submissions.size(), groupId);

        Group group = groupRepository.findById(groupId).orElseThrow(NonExistGroupException::new);
        log.info("[Service] Group details - station: {}, maxCapacity: {}, budget: {}",
                group.getStation(), group.getMaxCapacity(), group.getBudget());

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
        log.info("[Service] Calling Google Places API for station: {}", group.getStation());
        List<PlaceSummaryDto> placeSummaries = restaurantSearchService.findRestaurantsByStation(group.getStation());
        log.info("[Service] Found {} restaurant candidates for station: {}", placeSummaries.size(), group.getStation());

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

        log.info("=== [Service] Sending request to LLM server ===");
        log.info("[Service] LLM Request - people: {}, location: {}, budgetPerPerson: {}, candidates count: {}",
                request.getPeople(), request.getLocation(), request.getBudgetPerPerson(),
                request.getCandidates() != null ? request.getCandidates().size() : 0);
        log.debug("[Service] Full LLM request: {}", request);

        AiGenerateResponse aiResponse = webClient.post()
                .uri("/generate")
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                .accept(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .doOnNext(errorBody -> {
                                    log.error("=== [Service] LLM Server Error ===");
                                    log.error("[Service] Status Code: {}", response.statusCode());
                                    log.error("[Service] Error Response Body: {}", errorBody);
                                })
                                .map(errorBody -> new RuntimeException("LLM Server Error (" + response.statusCode() + "): " + errorBody))
                )
                .bodyToMono(AiGenerateResponse.class)
                .doOnSuccess(resp -> {
                    log.info("=== [Service] LLM response received successfully ===");
                    log.info("[Service] Number of results: {}", resp != null && resp.getResults() != null ? resp.getResults().size() : 0);
                    log.debug("[Service] Full response: {}", resp);
                })
                .doOnError(error -> {
                    log.error("=== [Service] LLM request failed ===", error);
                    log.error("[Service] Error type: {}", error.getClass().getName());
                    log.error("[Service] Error message: {}", error.getMessage());
                })
                .block();

        AiGenerateResponse aiGenerateResponse = new AiGenerateResponse(aiResponse.getResults());
        log.info("=== [Service] totalSubmit completed successfully for groupId: {} ===", groupId);

        return new FinalResponseDto(aiGenerateResponse, null);
    }
}
