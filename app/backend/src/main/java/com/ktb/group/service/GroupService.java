package com.ktb.group.service;

import com.ktb.group.domain.Group;
import com.ktb.group.dto.GroupFinalMeta;
import com.ktb.group.dto.TempAggregation;
import com.ktb.group.exception.NonExistGroupException;
import com.ktb.group.repository.GroupRepository;
import com.ktb.user.Repository.UserRepository;
import com.ktb.user.domain.UserIdentifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private GroupRepository groupRepository;
    private UserRepository userRepository;

    public TempAggregation getAggregation(Long groupId, Long ownerId) {
        Group aggregation =
                groupRepository.findByIdAndGroupOwner_Id(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        return TempAggregation.from(aggregation);
    }

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

    public GroupFinalMeta getGroupCompletionMeta(Long groupId, Long ownerId) {
        Group group =
                groupRepository.findByIdAndGroupOwner_Id(groupId, ownerId)
                        .orElseThrow(NonExistGroupException::new);

        group.validateAllSubmitted();

        return GroupFinalMeta.from(group);
    }
}
