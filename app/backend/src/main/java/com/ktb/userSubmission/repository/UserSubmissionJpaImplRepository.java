package com.ktb.userSubmission.repository;

import com.ktb.userSubmission.domain.UserSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//@Repository
public class UserSubmissionJpaImplRepository implements UserSubmissionRepository {

    @Autowired
    UserSubmissionJpaImplRepository userSubmissionJpaImplRepository;

    public Optional<UserSubmission> save(UserSubmission userSubmission){
        return userSubmissionJpaImplRepository.save(userSubmission);
    }

    public Optional<UserSubmission> findById(Long id){
        return userSubmissionJpaImplRepository.findById(id);
    }

    public Optional<UserSubmission> findByNickname(String nickname){
        return userSubmissionJpaImplRepository.findByNickname(nickname);
    }

    public List<UserSubmission> findAllByGroupId(Long groupId){
        return userSubmissionJpaImplRepository.findAllByGroupId(groupId);
    }




}
