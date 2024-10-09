package com.example.onemonth.domain.user;

import com.example.onemonth.domain.user.dto.*;
import com.example.onemonth.global.exception.CustomException;
import com.example.onemonth.global.exception.ErrorCode;
import com.example.onemonth.global.jwt.JwtUtil;
import com.example.onemonth.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SignUpResponseDto createUser(SignUpRequestDto requestDto) {

        String password = passwordEncoder.encode(requestDto.getPassword());
        if(userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        // User 객체 생성
        User user = User.builder()
                .username(requestDto.getUsername())
                .password(password)
                .nickname(requestDto.getNickname())
                .role(UserRole.USER)  // USER 권한 설정
                .build();

        // User 저장
        userRepository.save(user);

        // 사용자 권한 리스트 생성
        List<AuthorityDto> authorities = List.of(new AuthorityDto(user.getRole().getAuthority()));

        // 저장된 유저 정보로 SignUpResponseDto 생성
        return SignUpResponseDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .authorities(authorities)  // UserRole 리스트로 반환
                .build();
    }

    public SignResponseDto signUser(SignRequestDto requestDto) {

        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        String accessToken = jwtUtil.createAccessToken(user.getUsername(),user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername(),user.getRole());

        return SignResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public CheckDto checkSignIn(UserDetailsImpl userDetails) {
        CheckDto checkDto = new CheckDto();
        checkDto.setCheck(userDetails.getUsername() + " 님이 로그인 한 상태입니다.");

        return checkDto;
    }
}
