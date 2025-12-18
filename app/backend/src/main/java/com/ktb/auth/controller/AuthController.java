package com.ktb.auth.controller;

import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.auth.constant.MessageConstant;
import com.ktb.user.dto.CommonResponse;
import com.ktb.auth.dto.LoginRequest;
import com.ktb.auth.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (JWT 토큰 발급)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 사용자명 또는 비밀번호)")
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<Void>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보", required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class)))
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        // 1. 인증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // 2. JWT 생성
        SecurityUserAccount userDetails = (SecurityUserAccount) auth.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(userDetails.getAccount().getId());

        // 3. 쿠키 설정
        jwtTokenProvider.addTokenCookie(httpResponse, jwt);

        CommonResponse<Void> response = CommonResponse.of(MessageConstant.Success.LOGIN);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "로그아웃", description = "JWT 토큰을 만료시킵니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletResponse httpResponse) {
        jwtTokenProvider.expireTokenCookie(httpResponse);
        return ResponseEntity.ok(CommonResponse.of(MessageConstant.Success.LOGOUT));
    }
}
