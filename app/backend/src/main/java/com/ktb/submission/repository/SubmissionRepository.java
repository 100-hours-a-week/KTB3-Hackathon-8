package com.ktb.submission.repository;

import com.ktb.submission.domain.Submission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findAllByGroupId(Long groupId);

    Optional<Submission> findByNickname(String nickname);
}
