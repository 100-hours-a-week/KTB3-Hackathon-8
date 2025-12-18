package com.ktb.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // CSRF 토큰 보안 스킴 정의
        SecurityScheme csrfSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-XSRF-TOKEN")
                .description("CSRF Token (먼저 GET /api/v1/csrf를 호출하여 토큰을 쿠키로 받은 후, 이 헤더에 토큰 값을 입력하세요)");

        // JWT 토큰 보안 스킴 정의 (선택적)
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰 (인증이 필요한 엔드포인트에만 사용)");

        return new OpenAPI()
                .info(new Info()
                        .title("Yogiyo Boss API")
                        .description("""
                                KTB Hackathon Project - Group Delivery Service API

                                ### CSRF 토큰 사용 방법:
                                1. GET /api/v1/csrf 호출 → 응답에서 'token' 값 복사
                                2. Swagger UI 우측 상단 'Authorize' 버튼 클릭
                                3. 'X-XSRF-TOKEN' 필드에 복사한 토큰 값 입력
                                4. 이후 모든 POST/PUT/DELETE 요청에 자동으로 토큰이 포함됩니다
                                """)
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("CSRF-Token", csrfSecurityScheme)
                        .addSecuritySchemes("Bearer-JWT", jwtSecurityScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("CSRF-Token")  // 모든 요청에 CSRF 토큰 요구
                );
    }
}
