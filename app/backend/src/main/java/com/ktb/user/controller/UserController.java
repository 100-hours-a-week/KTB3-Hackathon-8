package com.ktb.user.controller;

import com.ktb.user.dto.UserDto;
import com.ktb.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 가입 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
    })
    @PostMapping
    public ResponseEntity<UserDto> signup(
            @RequestBody(description = "사용자 정보", required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class)))
            @org.springframework.web.bind.annotation.RequestBody UserDto userDto
    ) {
        userService.create(userDto);
        return ResponseEntity.ok().body(userDto);
    }
}
