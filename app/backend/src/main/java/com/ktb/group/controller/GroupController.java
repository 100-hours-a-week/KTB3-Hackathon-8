package com.ktb.group.controller;

import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.group.dto.TempAggregation;
import com.ktb.group.dto.request.CreateGroupRequest;
import com.ktb.group.dto.response.CreateGroupIdResponse;
import com.ktb.group.dto.response.GroupMeta;
import com.ktb.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group", description = "그룹 관리 API")
@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @Operation(summary = "모임 그룹을 생성", description = "모임 그룹을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 생성 성공"),
    })
    @PostMapping()
    public ResponseEntity<CreateGroupIdResponse> createGroup(
            @RequestBody CreateGroupRequest groupRequest,
            @AuthenticationPrincipal SecurityUserAccount principal
    ) {
        Long createdId = groupService.createGroup(groupRequest, principal.getAccount().getId());

        CreateGroupIdResponse response = new CreateGroupIdResponse(createdId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 정보 조회", description = "그룹의 정보 현황과 총무 여부를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TempAggregation.class))),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupMeta> getGroupInfo(
            @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
            @AuthenticationPrincipal SecurityUserAccount principal
    ) {
        Long requestUserId = 0L;
        if (principal != null && principal.getAccount() != null) {
            requestUserId = principal.getAccount().getId();
        }
        GroupMeta metaInfo = groupService.getGroupInfo(requestUserId, groupId);

        return ResponseEntity.ok(metaInfo);
    }

    @Operation(summary = "그룹 집계 조회", description = "그룹의 제출 현황을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TempAggregation.class))),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}/{ownerId}/aggregation")
    public ResponseEntity<TempAggregation> getAggregation(
            @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "그룹 오너 ID", required = true) @PathVariable Long ownerId
    ) {
        TempAggregation aggregation = groupService.getAggregation(groupId, ownerId);
        return ResponseEntity.ok().body(aggregation);
    }

    @Operation(summary = "그룹 멤버 제출", description = "사용자를 그룹에 제출합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 사용자를 찾을 수 없음")
    })
    @PostMapping("/{groupId}/{ownerId}/submissions")
    public ResponseEntity<Void> submit(
            @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "그룹 오너 ID", required = true) @PathVariable Long ownerId,
            @Parameter(description = "제출할 사용자 닉네임", required = true) @RequestParam(required = true) String userNickname
    ) {
        groupService.submitMember(groupId, ownerId, userNickname);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "초대 URL 생성", description = "그룹 초대 URL을 생성하고 리다이렉트합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "303", description = "리다이렉트 성공"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}/{ownerId}/invite-url")
    public ResponseEntity<Void> getInviteUrl(
            @Parameter(description = "그룹 ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "그룹 오너 ID", required = true) @PathVariable Long ownerId,
            HttpServletRequest request
    ) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + "/api/v1/";
        String inviteUrl = groupService.buildGroupInviteUrl(baseUrl, groupId, ownerId);

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, inviteUrl)
                .build();
    }
}
