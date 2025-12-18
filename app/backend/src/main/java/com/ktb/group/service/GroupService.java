package com.ktb.group.service;

import com.ktb.group.domain.Group;
import com.ktb.group.dto.GroupFinalMeta;
import com.ktb.group.dto.TempAggregation;
import com.ktb.group.exception.NonExistGroupException;
import com.ktb.group.repository.GroupRepository;
import com.ktb.user.Repository.UserRepository;
import com.ktb.user.domain.UserIdentifier;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private GroupRepository groupRepository;
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    @Lock(value = LockModeType.PESSIMISTIC_READ)
    public TempAggregation getAggregation(Long groupId, Long ownerId) {
        Group aggregation =
                groupRepository.findByIdAndGroupOwner_Id(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        return TempAggregation.from(aggregation);
    }

    @Transactional
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public void submitMember(Long groupId, Long ownerId, String userNickname) {
        Group aggregation =
                groupRepository.findByIdAndGroupOwner_Id(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        UserIdentifier submitUser = userRepository.findUserIdentifierByNickName(userNickname).orElseThrow();

        aggregation.submitMember(submitUser);
    }

    public String buildGroupInviteUrl(String baseUrl, Long groupId, Long ownerId) {
        return String.format("%s/group/%d/%d", baseUrl, groupId, ownerId);
    }

    @Transactional(readOnly = true)
    @Lock(value = LockModeType.PESSIMISTIC_READ)
    public GroupFinalMeta getGroupCompletionMeta(Long groupId, Long ownerId) {
        Group group =
                groupRepository.findByIdAndGroupOwner_Id(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        group.validateAllSubmitted();

        return GroupFinalMeta.from(group);
    }
}
