package com.ktb.auth.filter;

import com.ktb.auth.adapter.SecurityUserAccount;
import com.ktb.auth.config.SecurityProperties;
import com.ktb.auth.service.CustomUserDetailService;
import com.ktb.auth.util.JwtTokenProvider;
import com.ktb.group.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final CustomUserDetailService userDetailsService;
    private final JwtTokenProvider jwtProvider;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, IOException {
        String token = jwtProvider.extractTokenFromRequest(request).orElse(null);

        if (token == null) {
            filterChain.doFilter(request, response);

            return;
        }

        if (!jwtProvider.validateToken(token)) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);

            return;
        }

        try {
            authenticateWithJwt(token);
        } catch (AuthenticationException e) {
            log.info("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithJwt(String jwt) {
        Long userId = jwtProvider.getUserIdFromToken(jwt);

        SecurityUserAccount userDetails = (SecurityUserAccount) userDetailsService.loadUserById(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // public 경로 (permitAll + anonymous)는 JWT 필터를 거치지 않음
        boolean isPublic = securityProperties.isPublicEndpoint(request);

        if (isPublic && log.isDebugEnabled()) {
            String type = securityProperties.isPermitAllEndpoint(request)
                    ? "permitAll"
                    : "anonymous";
            log.debug("Skipping JWT filter for {} endpoint: {}",
                    type, request.getRequestURI());
        }

        return isPublic;
    }
}
