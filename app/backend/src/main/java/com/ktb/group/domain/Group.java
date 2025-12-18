package com.ktb.group.domain;

import com.ktb.group.exception.FullJoinMemberException;
import com.ktb.group.exception.GroupSubmissionNotCompletedException;
import com.ktb.user.domain.UserIdentifier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "join_group")
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserIdentifier groupOwner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_members")
    private Set<UserIdentifier> submitMembers;

    private int budget;
    private String station;

    public static Group create(int maxCapacity, UserIdentifier groupOwner, Set<UserIdentifier> submitMembers, int budget, String station) {
        if(!submitMembers.contains(groupOwner)) {
            submitMembers.add(groupOwner);
        }

        return new Group(null, maxCapacity, groupOwner, submitMembers, budget, station);
    }

    public static Group create(int maxCapacity, UserIdentifier groupOwner, int budget, String station) {
        Set<UserIdentifier> joinMembers = new HashSet<>();
        joinMembers.add(groupOwner);

        return new Group(null, maxCapacity, groupOwner, joinMembers, budget, station);
    }

    public void submitMember(UserIdentifier user) {
        if (submitMembers.size() >= maxCapacity) {
            throw new FullJoinMemberException();
        }

        submitMembers.add(user);
    }

    public void validateAllSubmitted() {
        if(! (submitMembers.size() == maxCapacity) ) {
            throw new GroupSubmissionNotCompletedException();
        }
    }
}
