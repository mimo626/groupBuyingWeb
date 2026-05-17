package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.AuthRequest;
import com.example.groupbuyingweb.domain.dto.response.AuthResponse;
import com.example.groupbuyingweb.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Validated
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(로그인 아이디 미입력) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/login-ids/check") // 수정: 아이디 중복확인 URL을 복수형 자원 기준으로 정리
    public ApiResponse<AuthResponse.DuplicateCheck> checkLoginId(
            @RequestParam("loginId")
            @NotBlank(message = "로그인 아이디를 입력해주세요.")
            String loginId
    ) {
        AuthResponse.DuplicateCheck response = authService.checkLoginId(loginId);
        return ApiResponse.success(response);
    }

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(닉네임 미입력) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/nicknames/check") // 수정: 닉네임 중복확인 URL을 복수형 자원 기준으로 정리
    public ApiResponse<AuthResponse.DuplicateCheck> checkNickname(
            @RequestParam("nickname")
            @NotBlank(message = "닉네임을 입력해주세요.")
            String nickname
    ) {
        AuthResponse.DuplicateCheck response = authService.checkNickname(nickname);
        return ApiResponse.success(response);
    }

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공(회원 자원 생성 완료) 시 201 Created
    // 실패(필수값 누락, 유효성 에러) 시 400 Bad Request
    // 실패(아이디 또는 닉네임 중복) 시 409 Conflict
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @PostMapping("/signup")
    public ApiResponse<AuthResponse.SignupResult> signup(
            @Valid @RequestBody AuthRequest.Signup request
    ) {
        AuthResponse.SignupResult response = authService.signup(request);
        return ApiResponse.success(response);
    }

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(필수값 누락, 유효성 에러) 시 400 Bad Request
    // 실패(로그인 아이디 또는 비밀번호 불일치) 시 401 Unauthorized
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @PostMapping("/login")
    public ApiResponse<AuthResponse.LoginResult> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpSession session
    ) {
        AuthResponse.LoginResult response = authService.login(request, session);
        return ApiResponse.success(response);
    }

    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpSession session
    ) {
        authService.logout(session);
        return ApiResponse.success(null);
    }
}