package com.ktb.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CSRF", description = "CSRF 토큰 관련 API")
@RestController
@RequestMapping("/api/v1/csrf")
public class CsrfController {

    @Operation(
            summary = "CSRF 토큰 생성",
            description = "CSRF 토큰을 생성하고 쿠키에 설정합니다. 프론트엔드에서 초기 로드 시 호출하여 토큰을 받을 수 있습니다."
    )
    @GetMapping
    public CsrfToken getCsrfToken(CsrfToken token) {
        // Spring Security가 자동으로 CsrfToken을 파라미터로 주입
        // 이 엔드포인트 호출만으로 CSRF 토큰이 쿠키에 설정됨
        // 반환값 없이 200 OK만 반환
        return token;
    }
}
