package com.ktb.user.service;

import com.ktb.user.domain.UserIdentifier;
import com.ktb.user.dto.UserDto;
import com.ktb.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 -> 유저 생성
    @Transactional
    public void create(UserDto userDto) {
        // 아이디(username) 중복성 검사
        Optional<UserIdentifier> existingUser = userRepository.findByUsername(userDto.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException();
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(userDto.getPassword());

        UserIdentifier userIdentifier = new UserIdentifier(userDto.getUsername(), userDto.getNickname(), encryptedPassword);
        userRepository.save(userIdentifier);
    }

}
