package com.example.onemonth.domain.user;

import com.example.onemonth.domain.user.dto.*;
import com.example.onemonth.global.common.CommonResponse;
import com.example.onemonth.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/users/signup")
    public ResponseEntity<CommonResponse> createUser(@RequestBody SignUpRequestDto requestDto) {
        SignUpResponseDto responseDto = userService.createUser(requestDto);
        CommonResponse response = new CommonResponse<>("회원가입 성공", 201, responseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/sign")
    public ResponseEntity<CommonResponse> signUser(@RequestBody SignRequestDto requestDto) {
        SignResponseDto responseDto = userService.signUser(requestDto);
        CommonResponse response = new CommonResponse<>("로그인 성공", 200, responseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<CommonResponse> checkUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        CheckDto responseDto = userService.checkSignIn(userDetails);
        CommonResponse response = new CommonResponse<>("로그인 체크", 200, responseDto);

        return ResponseEntity.ok(response);
    }
}
