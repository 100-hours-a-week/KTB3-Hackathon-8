package com.ktb.user.controller;

import com.ktb.user.dto.UserDto;
import com.ktb.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    // 회원 가입
    @PostMapping
    public ResponseEntity<UserDto> signup(@RequestBody UserDto userDto) {

        try {
            userService.create(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (Exception e) {
            throw new IllegalArgumentException("중복된 아이디입니다.");
        }
    }

    // 로그인

}
