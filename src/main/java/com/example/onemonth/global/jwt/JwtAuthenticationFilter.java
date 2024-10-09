package com.example.onemonth.global.jwt;

import com.example.onemonth.domain.user.UserRole;
import com.example.onemonth.global.common.CommonErrorResponse;
import com.example.onemonth.global.exception.CustomException;
import com.example.onemonth.global.exception.ErrorCode;
import com.example.onemonth.global.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization"; // Header Key 값

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // GET 요청의 /users/profile/** 경로 필터링 제외
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String headerValue = request.getHeader("Authorization");
            if (headerValue == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            String tokenValue = jwtUtil.extractBearerTokenFromHeader(request, AUTHORIZATION_HEADER);

            jwtUtil.validateToken(tokenValue);

            String userId = jwtUtil.getUsernameFromToken(tokenValue);
            UserRole role = jwtUtil.getRoleFromToken(tokenValue);

            setAuthentication(userId, role);

        } catch (CustomException e) {
            handleException(response, e.getMessage(), e.getStatusCode());
            return;
        } catch (Exception e) {
            handleException(response, "Authentication Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        filterChain.doFilter(request,response);

    }

    public void setAuthentication(String userId, UserRole role) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(userId, role);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String userId, UserRole role) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        Collection<? extends GrantedAuthority> authorities = getAuthorities(role);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserRole role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }


    private void handleException(HttpServletResponse res, String message, HttpStatus httpStatus) throws IOException {
        res.setStatus(httpStatus.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        CommonErrorResponse errorResponse = CommonErrorResponse.builder()
                .message(message)
                .error(httpStatus.getReasonPhrase())
                .statusCode(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.registerModule(javaTimeModule);

        res.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
