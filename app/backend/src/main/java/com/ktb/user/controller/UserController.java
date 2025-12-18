package com.ktb.user.controller;

import com.ktb.user.dto.UserDto;
import com.ktb.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "회원 가입")
    @ApiResponse(responseCode = "200", description = "회원 가입 성공")
    public ResponseEntity<UserDto> signup(@RequestBody UserDto userDto) {
        userService.create(userDto);
        return ResponseEntity.ok().body(userDto);
    }


}
