package com.ktb.group.repository;

import com.ktb.group.domain.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByIdAndMembers_User_IdAndMembers_IsOwner(Long groupId, Long ownerId, boolean isOwner);
}
