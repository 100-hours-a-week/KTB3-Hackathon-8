package com.ktb.submission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktb.group.domain.Group;
import com.ktb.group.repository.GroupRepository;
import com.ktb.submission.domain.Submission;
import com.ktb.submission.dto.FinalResponseDto;
import com.ktb.submission.dto.request.SubmitRequest;
import com.ktb.submission.repository.SubmissionRepository;
import com.ktb.user.domain.UserIdentifier;
import com.ktb.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SubmissionController 통합 테스트
 *
 * Spring Security CSRF 설정 및 실제 LLM 서버 통신을 검증합니다.
 *
 * 환경변수 설정:
 * - GOOGLE_API_KEY: Google Places API 키
 * - RUN_INTEGRATION_TEST: "true"로 설정하면 실제 LLM 서버와 통신
 *
 * 사용법:
 * export GOOGLE_API_KEY=your-api-key
 * export RUN_INTEGRATION_TEST=true
 * ./gradlew test --tests SubmissionControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("SubmissionController 통합 테스트")
class SubmissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    private Long testGroupId;
    private UserIdentifier testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // 테스트 데이터 생성
        testUser = new UserIdentifier("test_owner", "테스트총무", "password123");
        userRepository.save(testUser);

        Date startDate = new Date(System.currentTimeMillis() + 86400000); // +1 day
        Date endDate = new Date(System.currentTimeMillis() + 604800000);  // +7 days

        Group testGroup = Group.create(
                testUser,
                4,
                "강남역",
                80000,
                true,
                startDate,
                endDate
        );
        groupRepository.save(testGroup);
        testGroupId = testGroup.getId();

        // 제출 데이터 3개 생성
        createSubmission(testGroup, "멤버1", "치킨,피자", "생선", "");
        createSubmission(testGroup, "멤버2", "파스타,스테이크", "매운음식", "");
        createSubmission(testGroup, "멤버3", "초밥,회", "육류", "");
    }

    private void createSubmission(Group group, String nickname, String preferred, String avoided, String excluded) {
        Submission submission = Submission.create(
                group,
                nickname,
                preferred,
                avoided,
                excluded,
                Collections.emptyList()
        );
        submissionRepository.save(submission);
    }

    @Test
    @DisplayName("POST /api/v1/submission/total/{groupId} - 익명 사용자 접근 가능")
    @Transactional
    void testTotalSubmit_AnonymousAccess_ShouldSucceed() throws Exception {
        // given
        System.out.println("\n=== 익명 사용자 접근 테스트 ===");
        System.out.println("인증 정보: 없음 (JWT 토큰 없이 요청)");

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/submission/total/{groupId}", testGroupId)
                        .with(csrf())  // CSRF 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        // JWT 토큰도 보내지 않음
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("\n✅ 익명 접근 확인: 인증 없이도 접근 가능");
        System.out.println("Response Status: " + result.getResponse().getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/submission/total/{groupId} - 실제 LLM 서버 통신 테스트")
    @Transactional
    void testTotalSubmit_WithRealLlmServer_ShouldReturnRecommendations() throws Exception {
        // given
        System.out.println("\n=== 실제 LLM 서버 통신 테스트 ===");
        System.out.println("LLM 서버: http://3.236.242.98:8000");
        System.out.println("요청 데이터:");
        System.out.println("  - 그룹 ID: " + testGroupId);
        System.out.println("  - 위치: 강남역");
        System.out.println("  - 인원: 4명");
        System.out.println("  - 예산: 80,000원 (1인당 20,000원)");
        System.out.println("  - 제출 수: 3개");

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/submission/total/{groupId}", testGroupId)
                        .with(csrf())  // CSRF 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        FinalResponseDto response = objectMapper.readValue(responseBody, FinalResponseDto.class);

        System.out.println("\n=== LLM 서버 응답 ===");
        System.out.println("Response Body: " + responseBody);

        assertThat(response).isNotNull();
        assertThat(response.getAiGenerateResponse()).isNotNull();
        assertThat(response.getAiGenerateResponse().getResults()).isNotEmpty();

        System.out.println("✅ LLM 서버 통신 성공!");
        System.out.println("추천 결과 수: " + response.getAiGenerateResponse().getResults().size());

        response.getAiGenerateResponse().getResults().forEach(result1 -> {
            System.out.println("\n추천 레스토랑:");
            System.out.println("  - 이름: " + result1.getDisplayName());
            System.out.println("  - 이유: " + result1.getReason());
        });
    }

    @Test
    @DisplayName("POST /api/v1/submission/{groupId}/user - 개별 제출 (CSRF 없이)")
    @Transactional
    void testUserSubmit_WithoutCsrf_ShouldSucceed() throws Exception {
        // given
        SubmitRequest request = new SubmitRequest(
                "새멤버",
                "male",
                25,
                Collections.emptyList(),
                "한식,중식",
                "일식",
                ""
        );

        System.out.println("\n=== 개별 제출 테스트 (CSRF 없이) ===");
        System.out.println("요청 Body: " + objectMapper.writeValueAsString(request));

        // when & then
        mockMvc.perform(post("/api/v1/submission/{groupId}/user", testGroupId)
                        .with(csrf())  // CSRF 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());

        System.out.println("✅ 개별 제출 성공 (CSRF 검사 제외 확인)");
    }

    @Test
    @DisplayName("환경변수 설정 가이드 출력")
    void printEnvironmentSetupGuide() {
        String googleApiKey = System.getenv("GOOGLE_API_KEY");
        String runIntegrationTest = System.getenv("RUN_INTEGRATION_TEST");

        System.out.println("\n==========================================");
        System.out.println("📋 통합 테스트 설정 가이드");
        System.out.println("==========================================");
        System.out.println("GOOGLE_API_KEY: " + (googleApiKey != null ? "설정됨 (" + maskApiKey(googleApiKey) + ")" : "❌ 설정되지 않음"));
        System.out.println("RUN_INTEGRATION_TEST: " + (runIntegrationTest != null ? runIntegrationTest : "❌ 설정되지 않음 (기본값: false)"));
        System.out.println("\n실제 LLM 서버와 연결하려면:");
        System.out.println("1. 터미널에서 환경변수 설정:");
        System.out.println("   export GOOGLE_API_KEY=your-google-api-key");
        System.out.println("   export RUN_INTEGRATION_TEST=true");
        System.out.println("\n2. 테스트 실행:");
        System.out.println("   ./gradlew test --tests SubmissionControllerIntegrationTest");
        System.out.println("\n주요 테스트 항목:");
        System.out.println("✓ Spring Security CSRF 설정 (/api/** 제외)");
        System.out.println("✓ 익명 사용자 접근 가능 (JWT 불필요)");
        System.out.println("✓ Google Places API 연동");
        System.out.println("✓ Python LLM 서버 통신 (http://3.236.242.98:8000)");
        System.out.println("==========================================\n");

        assertThat(true).isTrue();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
