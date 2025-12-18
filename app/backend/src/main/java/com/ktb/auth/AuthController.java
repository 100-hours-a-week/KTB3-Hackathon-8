package com.ktb.auth;

import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.auth.constant.MessageConstant;
import com.ktb.auth.dto.CommonResponse;
import com.ktb.auth.dto.LoginRequest;
import com.ktb.auth.util.JwtTokenProvider;
import com.ktb.user.dto.UserDto;
import com.ktb.auth.constant.MessageConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공(JWT 토큰 발급)")
    public ResponseEntity<CommonResponse<Void>> login(@Valid@RequestBody LoginRequest request,
                                         HttpServletResponse httpResponse) {
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

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletResponse httpResponse) {
        jwtTokenProvider.expireTokenCookie(httpResponse);

        return ResponseEntity.ok(CommonResponse.of(MessageConstant.Success.LOGOUT));
    }
}
