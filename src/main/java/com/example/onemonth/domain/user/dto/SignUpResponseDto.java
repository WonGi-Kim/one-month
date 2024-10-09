package com.example.onemonth.domain.user.dto;

import com.example.onemonth.domain.user.UserRole;
import lombok.*;

import java.util.List;

// 사용자 정보 DTO
@Getter
@Setter
@NoArgsConstructor
public class SignUpResponseDto {

    private String username;
    private String nickname;
    private List<AuthorityDto> authorities;

    @Builder
    public SignUpResponseDto(String username, String nickname, List<AuthorityDto> authorities) {
        this.username = username;
        this.nickname = nickname;
        this.authorities = authorities;
    }
}
