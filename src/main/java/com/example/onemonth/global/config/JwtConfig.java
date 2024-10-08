package com.example.onemonth.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {
    // lombok 아님
    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.access-expire-time}")
    private long tokenExpiration;

    @Value("${jwt.refresh-expire-time}")
    private long refreshTokenExpiration;
}
