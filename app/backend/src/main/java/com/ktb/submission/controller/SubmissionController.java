package com.ktb.submission.controller;

import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.submission.dto.FinalResponseDto;
import com.ktb.submission.dto.request.SubmitRequest;
import com.ktb.submission.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Submission", description = "제출 관리 API")
@RestController
@RequestMapping("/api/v1/submission")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    @Operation(summary = "멤버 개별 제출", description = "그룹 멤버가 개별적으로 메뉴를 제출합니다. 총무는 닉네임 정보를 입력하지 않아도 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음")
    })
    @PostMapping("/{groupId}/user")
    public ResponseEntity<Void> userSubmit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "제출 정보", required = true,
                    content = @Content(schema = @Schema(implementation = SubmitRequest.class)))
            @RequestBody SubmitRequest submission,
            @PathVariable Long groupId,
            @AuthenticationPrincipal SecurityUserAccount principal
            ) {
        if (principal != null && principal.getAccount() != null) {
            submission = submission.withNickname(principal.getAccount().getNickname());
        }
        submissionService.userSubmit(groupId, submission);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "총무 통합 제출", description = "총무가 그룹의 모든 제출을 통합하여 프롬프트를 생성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통합 제출 성공",
                    content = @Content(schema = @Schema(implementation = FinalResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "모든 멤버가 제출하지 않음")
    })
    @PostMapping("/total/{groupId}")
    public ResponseEntity<FinalResponseDto> totalSubmit(
            @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId
    ) {
        FinalResponseDto promptResponse = submissionService.totalSubmit(groupId);
        return ResponseEntity.ok().body(promptResponse);
    }
}
