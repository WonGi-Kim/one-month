package com.example.onemonth;

import com.example.onemonth.domain.user.UserRole;
import com.example.onemonth.global.config.JwtConfig;
import com.example.onemonth.global.exception.CustomException;
import com.example.onemonth.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtUtil jwtUtil;

    private final String userName = "testUser";
    private final UserRole userRole = UserRole.USER;

    @BeforeEach
    void setUp() {
        when(jwtConfig.getTokenExpiration()).thenReturn(60000L); // 1분
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(1209600000L); // 2주

        when(jwtConfig.getSecretKey()).thenReturn(Base64.getEncoder().encodeToString("secretKeysecretKeysecretKeysecretKey".getBytes())); // 256비트 비밀키 설정

        // JwtUtil 인스턴스 초기화
        jwtUtil = new JwtUtil(jwtConfig);
    }

    /**
     * - 목적 : Access Token 생성 기능을 테스트
     * - 검증 : 생성된 Access Token이 null이 아니고, 유효한지 검증
     */
    @Test
    void testCreateAccessToken() {
        String accessToken = jwtUtil.createAccessToken(userName, userRole);
        assertNotNull(accessToken);
        assertTrue(jwtUtil.validateToken(accessToken));
    }

    /**
     * - 목적 : Refresh Token 생성 기능을 테스트
     * - 검증 : 생성된 Refresh Token이 null이 아니고, 유효한지 검증
     */
    @Test
    void testCreateRefreshToken() {
        String refreshToken = jwtUtil.createRefreshToken(userName, userRole);
        assertNotNull(refreshToken);
        assertTrue(jwtUtil.validateToken(refreshToken));
    }

    /**
     * - 목적 : 만료된 Access Token의 유효성 검증을 테스트
     * - 검증 : Access Token을 생성한 후, 61초 대기하여 만료시킨 후, 이를 검증할 때 CustomException이 발생하는지 확인
     */
    @Test
    void testValidateTokenWithExpiredToken() throws InterruptedException {
        String accessToken = jwtUtil.createAccessToken(userName, userRole);

        // Access Token이 만료되도록 대기
        Thread.sleep(61000); // 61초 대기

        // 만료된 Access Token 검증
        assertThrows(CustomException.class, () -> jwtUtil.validateToken(accessToken));
    }

    /**
     * - 목적 : Access Token에서 사용자 이름을 추출하는 기능을 테스트
     * - 검증 : Access Token에서 추출한 사용자 이름이 기대하는 사용자 이름과 일치하는지 확인
     */
    @Test
    void testGetUsernameFromToken() {
        String accessToken = jwtUtil.createAccessToken(userName, userRole);
        String extractedUsername = jwtUtil.getUsernameFromToken(accessToken);
        assertEquals(userName, extractedUsername);
    }

    /**
     * - 목적 : Access Token에서 사용자 역할을 추출하는 기능을 테스트
     * - 검증 : Access Token에서 추출한 역할이 기대하는 역할과 일치하는지 확인
     */
    @Test
    void testGetRoleFromToken() {
        String accessToken = jwtUtil.createAccessToken(userName, userRole);
        UserRole extractedRole = jwtUtil.getRoleFromToken(accessToken);
        assertEquals(userRole, extractedRole);
    }

    /**
     * - 목적 : Refresh Token을 사용하여 Access Token을 생성하는 기능을 테스트
     * - 검증 : 생성된 Access Token이 null이 아니고, 유효한지 검증
     */
    @Test
    void testCreateAccessTokenFromRefresh() {
        String refreshToken = jwtUtil.createRefreshToken(userName, userRole);
        String accessToken = jwtUtil.createAccessTokenFromRefresh(refreshToken, userRole);
        assertNotNull(accessToken);
        assertTrue(jwtUtil.validateToken(accessToken));
    }

    /**
     * - 목적 : 유효하지 않은 Refresh Token을 사용할 때 Access Token 생성의 실패를 테스트
     * - 검증 : 잘못된 Refresh Token을 사용해 Access Token을 생성할 때 IllegalArgumentException이 발생하는지 확인
     */
    @Test
    void testCreateAccessTokenFromInvalidRefresh() {
        // 실패가 정상
        String invalidToken = "invalidToken";
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.createAccessTokenFromRefresh(invalidToken, userRole));
    }

    /**
     * - 목적 : HttpServletRequest에서 Bearer Token을 추출하는 기능을 테스트
     * - 검증 : Bearer Token을 성공적으로 추출하고, 유효한지 검증
     */
    @Test
    void testExtractBearerTokenFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtUtil.createAccessToken(userName, userRole));

        String extractedToken = jwtUtil.extractBearerTokenFromHeader(request, "Authorization");
        assertNotNull(extractedToken);
        assertTrue(jwtUtil.validateToken(extractedToken));
    }

    /**
     * - 목적 : Authorization 헤더에 Bearer가 없는 경우의 동작을 테스트
     * - 검증 : Bearer 접두사가 없는 Token이 포함된 Authorization 헤더를 처리할 때 CustomException이 발생하는지 확인
     */
    @Test
    void testExtractBearerTokenFromHeaderWithoutBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Invalid " + jwtUtil.createAccessToken(userName, userRole));

        assertThrows(CustomException.class, () -> jwtUtil.extractBearerTokenFromHeader(request, "Authorization"));
    }
}
