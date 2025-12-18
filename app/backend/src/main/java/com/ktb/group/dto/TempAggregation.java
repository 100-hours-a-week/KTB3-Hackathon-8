package com.ktb.group.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ktb.group.domain.Group;
import com.ktb.user.domain.UserIdentifier;
import java.util.List;

public record TempAggregation(
        @JsonProperty("submit_count")
        int submitCnt,
        @JsonProperty("total_user_count")
        int totalCnt,
        @JsonProperty("user_nickname_list")
        List<String> userList
) {
    public static TempAggregation from(Group group) {
        List<String> userList =
                group.getSubmitMembers()
                        .stream()
                        .map(UserIdentifier::getNickname)
                        .toList();
        return new TempAggregation(group.getSubmitMembers().size(), group.getMaxCapacity(), userList);
    }
}
