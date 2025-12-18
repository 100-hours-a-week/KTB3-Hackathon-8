package com.ktb.userSubmission.repository;

import com.ktb.userSubmission.domain.UserSubmission;

import java.util.List;
import java.util.Optional;

public interface UserSubmissionRepository {


    public Optional<UserSubmission> save(UserSubmission userSubmission);

    public Optional<UserSubmission> findById(Long id);

    public Optional<UserSubmission> findByNickname(String nickname);

    public List<UserSubmission> findAllByGroupId(Long groupId);


}
