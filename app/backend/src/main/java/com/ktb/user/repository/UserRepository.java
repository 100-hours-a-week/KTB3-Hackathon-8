package com.ktb.user.repository;

import com.ktb.user.domain.UserIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserIdentifier, Long> {
    Optional<UserIdentifier> findByUsername(String username);

    Optional<Object> findUserIdentifierByNickname(String userNickname);
}
