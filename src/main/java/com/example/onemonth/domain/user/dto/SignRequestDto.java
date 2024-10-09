package com.example.onemonth.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignRequestDto {
    private String username;
    private String password;

    public SignRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
