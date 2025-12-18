package com.ktb.auth.dto;

import io.jsonwebtoken.security.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "로그인 요청 구조")
public record LoginRequest(
        @Schema(description = "ID(Username)", example = "username")
        String username,

        @Schema(description = "패스워드", example = "Password12#$56")
        String password
) {
}
