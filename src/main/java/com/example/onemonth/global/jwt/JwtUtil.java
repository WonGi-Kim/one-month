package com.example.onemonth.global.jwt;

import com.example.onemonth.domain.user.UserRole;
import com.example.onemonth.global.common.CommonResponse;
import com.example.onemonth.global.config.JwtConfig;
import com.example.onemonth.global.exception.CustomException;
import com.example.onemonth.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // 필드
    private final ObjectMapper mapper = new ObjectMapper(); // Json 변환을 위함
    public static final String BEARER = "Bearer ";

    public static final String AUTHORIZATION = "auth"; // 사용자 권한 Key

    public final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; // 암호화 알고리즘

    private final long tokenExpiration;
    private final long refreshTokenExpiration;
    private final SecretKey secretKey;

    public JwtUtil(JwtConfig jwtConfig) {
        this.tokenExpiration = jwtConfig.getTokenExpiration();
        this.refreshTokenExpiration = jwtConfig.getRefreshTokenExpiration();
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }

    // 토큰 생성
    public String createAccessToken(String userName, UserRole userRole) {
        return createToken(userName, userRole, tokenExpiration);
    }

    public String createRefreshToken(String userName, UserRole userRole) {
        return createToken(userName, userRole, refreshTokenExpiration);
    }

    public String createToken(String userName, UserRole userRole, long tokenExpiration) {
        return BEARER + Jwts.builder()
                .setSubject(userName) // 토큰 발행 주체
                .claim(AUTHORIZATION, userRole.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)) // 토큰 만료 시간
                .signWith(secretKey, signatureAlgorithm)
                .compact();
    }

    /**
     * RefreshToken 을 사용하여 AccessToken 생성
     */
    public String createAccessTokenFromRefresh(String refreshToken,  UserRole userRole) {
        if (validateToken(refreshToken)) {
            String username = getUsernameFromToken(refreshToken);
            return createAccessToken(username, userRole);
        }
        throw new IllegalArgumentException("Refresh토큰이 유효하지 않음");
    }


    // 토큰 발급
    /**
     * 요구 사항에 따라 로그인 시
     * AccessToken + RefreshToken 을 Body로 제공
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access", accessToken);
        if (refreshToken != null) {
            tokens.put("refresh", refreshToken);
        }
        CommonResponse responseForm = new CommonResponse<>("Access Token, Refresh Token 바디에 설정 성공",200,tokens);

        String responseBody = mapper.writeValueAsString(responseForm);
        response.getWriter().write(responseBody);
    }

    // 토큰 확인
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.NOT_SUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.FALSE_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRATION);
        }
    }

    /**
     * 사용자 정보 추출
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 사용자 권한 추출
     */
    public UserRole getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return UserRole.valueOf(claims.get(AUTHORIZATION).toString());
    }

    /**
     * 사용자 이름 추출
     */
    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();  // JWT의 subject 필드에서 userName 추출
    }

    // 토큰 다루기
    /**
     * header에서 토큰 추출
     */
    public String extractBearerTokenFromHeader(HttpServletRequest request, String header) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return substringToken(bearerToken);
        }
        throw new CustomException(ErrorCode.HEADER_NOT_FOUND);
    }

    /**
     * BEARER 제거 후 토큰 반환
     */
    public String substringToken(String token) {
        return token.substring(BEARER.length()); // "Bearer " 접두사 제거
    }

}
