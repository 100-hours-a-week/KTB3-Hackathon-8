package com.ktb.group.controller;

import com.ktb.group.dto.TempAggregation;
import com.ktb.group.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {
    private GroupService groupService;

    @GetMapping("/{groupId}/{ownerId}/aggregation")
    public ResponseEntity<TempAggregation> getAggregation(
            @PathVariable Long groupId,
            @PathVariable Long ownerId
    ) {
        TempAggregation aggregation = groupService.getAggregation(groupId, ownerId);


        return ResponseEntity.ok().body(aggregation);
    }

    @PostMapping("/{groupId}/{ownerId}/submissions")
    public ResponseEntity<Void> submit(
            @PathVariable Long groupId,
            @PathVariable Long ownerId,
            @RequestParam(required = true) String userNickname
    ) {
        groupService.submitMember(groupId, ownerId, userNickname);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/{ownerId}/invite-url")
    public ResponseEntity<Void> getInviteUrl(
            @PathVariable Long groupId,
            @PathVariable Long ownerId,
            HttpServletRequest request
    ) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + "/api/v1/";

        String inviteUrl =
                groupService.buildGroupInviteUrl(baseUrl, groupId, ownerId);

        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, inviteUrl)
                .build();
    }
}
