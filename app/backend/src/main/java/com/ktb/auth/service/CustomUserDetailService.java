package com.ktb.auth.service;


import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.user.domain.UserIdentifier;
import com.ktb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserIdentifier userIdentifier = userRepository
                .findByUsername(username)
                .orElseThrow();
        return new SecurityUserAccount(userIdentifier);
    }

    /**
     * userId로 사용자 조회 (JWT 인증 시 JwtAuthenticationFilter 호출)
     */
    public UserDetails loadUserById(Long userId) {
        UserIdentifier userIdentifier = userRepository
                .findById(userId)
                .orElseThrow();
        return new SecurityUserAccount(userIdentifier);
    }
}
