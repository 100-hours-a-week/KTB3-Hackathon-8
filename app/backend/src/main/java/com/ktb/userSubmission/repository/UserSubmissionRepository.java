package com.ktb.userSubmission.repository;

import com.ktb.userSubmission.domain.UserSubmission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubmissionRepository extends JpaRepository<UserSubmission, Long> {
    List<UserSubmission> findAllByGroupId(Long groupId);

    Optional<UserSubmission> findByNickname(String nickname);
}
