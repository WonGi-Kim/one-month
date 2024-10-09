package com.example.onemonth.domain.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column
    private String nickname;

    @Setter
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserRole role;

    @Builder
    public User(String username, String nickname, String password, UserRole role) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
    }
}
