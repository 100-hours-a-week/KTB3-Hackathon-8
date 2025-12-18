package com.ktb.userSubmission.repository;

import com.ktb.userSubmission.domain.UserSubmission;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserSubmissionMemoryRepository implements UserSubmissionRepository{

    private static final Map<Long, UserSubmission> store = new HashMap<>();
    private static Long sequence = 0L;


    @Override
    public Optional<UserSubmission> save(UserSubmission userSubmission) {
        store.put(++sequence, userSubmission);
        return Optional.empty();
    }

    @Override
    public Optional<UserSubmission> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<UserSubmission> findByNickname(String nickname) {
        return store.values().stream()
                .filter(us -> us.getNickname().equals(nickname))
                .findFirst();
    }

    @Override
    public List<UserSubmission> findAllByGroupId(Long groupId) {
        return store.values().stream()
                .filter(us -> us.getGroupId().equals(groupId))
                .toList();
    }
}
