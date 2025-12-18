package com.ktb.auth.util;

import com.ktb.auth.config.JwtConfig;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtConfig cfg;
    private final ResourceLoader loader;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() {
        try {
            this.privateKey = readPrivate(cfg.getPrivateKeyPath());
            this.publicKey  = readPublic(cfg.getPublicKeyPath());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys", e);
        }
    }

    private PrivateKey readPrivate(String location) throws Exception {
        String pem = readPem(location, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
        byte[] der = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private PublicKey readPublic(String location) throws Exception {
        String pem = readPem(location, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
        byte[] der = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private String readPem(String loc, String begin, String end) throws IOException {
        Resource r = loader.getResource(loc);
        String s = new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return s.replace(begin, "")
                .replace(end, "")
                .replaceAll("\\s", "");
    }

    // ✅ JWT 생성 (userId)
    public String generateToken(Long userId) {
        Date now = new Date(System.currentTimeMillis());
        Date expireDate = new Date(now.getTime() + cfg.getAccessExpireMillis());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public void addTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(cfg.getAccessTokenName(), token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge((int)cfg.getAccessExpireSeconds())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expireTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cfg.getAccessTokenName(), "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)  // 쿠키 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // ✅ JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token) // Throws if invalid
                    .getPayload()
                    .getExpiration()
                    .after(new Date(System.currentTimeMillis()));

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> cfg.getAccessTokenName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
