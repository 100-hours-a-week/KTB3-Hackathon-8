package com.ktb.group.service;

import com.ktb.group.domain.Group;
import com.ktb.group.dto.GroupFinalMeta;
import com.ktb.group.dto.TempAggregation;
import com.ktb.group.dto.request.CreateGroupRequest;
import com.ktb.group.exception.NonExistGroupException;
import com.ktb.group.repository.GroupRepository;
import com.ktb.user.repository.UserRepository;
import com.ktb.user.domain.UserIdentifier;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Lock(value = LockModeType.PESSIMISTIC_READ)
    public TempAggregation getAggregation(Long groupId, Long ownerId) {
        Group aggregation =
                groupRepository.findByIdAndOwnerIdWithMembers(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        return TempAggregation.from(aggregation);
    }

    @Transactional
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public void submitMember(Long groupId, Long ownerId, String userNickname) {
        Group aggregation =
                groupRepository.findByIdAndMembers_User_IdAndMembers_IsOwner(groupId, ownerId, true)
                        .orElseThrow(NonExistGroupException::new);

        UserIdentifier submitUser = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userNickname));

        aggregation.submitMember(submitUser);
    }

    public String buildGroupInviteUrl(String baseUrl, Long groupId, Long ownerId) {
        return String.format("%s/group/%d/%d", baseUrl, groupId, ownerId);
    }

    @Transactional(readOnly = true)
    @Lock(value = LockModeType.PESSIMISTIC_READ)
    public GroupFinalMeta getGroupCompletionMeta(Long groupId, Long ownerId) {
        Group group =
                groupRepository.findByIdAndMembers_User_IdAndMembers_IsOwner(groupId, ownerId, true)
                        .orElseThrow(NonExistGroupException::new);

        group.validateAllSubmitted();

        return GroupFinalMeta.from(group);
    }

    public Long createGroup(CreateGroupRequest groupRequest) {
        UserIdentifier user = userRepository.findById(groupRequest.ownerId()).orElseThrow();
        //Long ownerId, Integer maxCapacity, String station, Integer budget
        Group group = Group.create(
                user,
                groupRequest.maxCapacity(),
                groupRequest.station(),
                groupRequest.budget(),
                groupRequest.hasScheduledDate(),
                groupRequest.startDate(),
                groupRequest.endDate()
        );

        return groupRepository.save(group).getId();
    }
}
