package com.ktb.group.domain;

import com.ktb.user.domain.UserIdentifier;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "group_member")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "group_member_seq"
    )
    @SequenceGenerator(
            name = "group_member_seq",
            sequenceName = "group_member_seq",
            allocationSize = 100
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserIdentifier user;

    private boolean isOwner;

    public static GroupMember create(Group group, UserIdentifier user, boolean isOwner) {
        return new GroupMember(null, group, user, isOwner);
    }

    public static GroupMember createOwner(Group group, UserIdentifier user) {
        return new GroupMember(null, group, user, true);
    }

    public static GroupMember createMember(Group group, UserIdentifier user) {
        return new GroupMember(null, group, user, false);
    }
}
