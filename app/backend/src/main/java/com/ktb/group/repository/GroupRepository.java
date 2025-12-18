package com.ktb.group.repository;

import com.ktb.group.domain.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByIdAndGroupOwner_Id(Long id, Long ownerId);
}
