package com.ktb.userSubmission.repository;

import com.ktb.userSubmission.domain.UserSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubmissionJpaRepository extends JpaRepository<UserSubmission, Long> {

    public List<UserSubmission> findAllByGroupId(Long groupId);


}
