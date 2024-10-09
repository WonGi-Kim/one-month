package com.example.onemonth.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckDto {
    private String check;

    public CheckDto(String check) {
        this.check = check;
    }
}
