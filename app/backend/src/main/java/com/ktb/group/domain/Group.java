package com.ktb.group.domain;

import com.ktb.group.exception.FullJoinMemberException;
import com.ktb.group.exception.GroupSubmissionNotCompletedException;
import com.ktb.user.domain.UserIdentifier;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "submit_group")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "group_seq"
    )
    @SequenceGenerator(
            name = "group_seq",
            sequenceName = "group_seq",
            allocationSize = 100
    )
    private Long id;

    @Column(name = "max_capacity")
    private int maxCapacity;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<GroupMember> members = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private UserIdentifier owner;

    private int budget;

    private String station;

    @Column(nullable = false)
    private boolean hasScheduledDate;

    public void submitMember(UserIdentifier user) {
        if (members.size() >= maxCapacity) {
            throw new FullJoinMemberException();
        }

        GroupMember groupMember = GroupMember.createMember(this, user);
        members.add(groupMember);
    }

    public void validateAllSubmitted() {
        if (members.size() != maxCapacity) {
            throw new GroupSubmissionNotCompletedException();
        }
    }

    public Set<UserIdentifier> getSubmitMembers() {
        return members.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toSet());
    }

    public static Group create(UserIdentifier owner, Integer maxCapacity, String station, Integer budget, boolean hasScheduledDate) {
        return new Group(null, maxCapacity, Collections.emptyList(), owner, budget, station, hasScheduledDate);
    }
}
