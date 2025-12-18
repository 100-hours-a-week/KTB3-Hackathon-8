package com.ktb.group.repository;

import com.ktb.group.domain.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByIdAndMembers_User_IdAndMembers_IsOwner(Long groupId, Long ownerId, boolean isOwner);

    @Query("SELECT g FROM Group g " +
           "JOIN FETCH g.members m " +
           "JOIN FETCH m.user " +
           "WHERE g.id = :groupId AND m.user.id = :ownerId AND m.isOwner = true")
    Optional<Group> findByIdAndOwnerIdWithMembers(@Param("groupId") Long groupId, @Param("ownerId") Long ownerId);
}
