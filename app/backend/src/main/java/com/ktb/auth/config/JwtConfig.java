package com.ktb.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String accessTokenName;
    private String refreshTokenName;
    private String privateKeyPath;
    private String publicKeyPath;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration accessExpire;

    @DurationUnit(ChronoUnit.DAYS)
    private Duration refreshExpire;

    public long getAccessExpireMillis() {
        return accessExpire.toMillis();
    }

    public long getRefreshExpireMillis() {
        return refreshExpire.toMillis();
    }

    public long getAccessExpireSeconds() {
        return accessExpire.toSeconds();
    }

    public long getRefreshExpireSeconds() {
        return refreshExpire.toSeconds();
    }
}
