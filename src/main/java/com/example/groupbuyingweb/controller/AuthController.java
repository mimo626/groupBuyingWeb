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

    @ResponseBody
    @GetMapping("/login-id/check")
    public ApiResponse<AuthResponse.DuplicateCheck> checkLoginId(
            @RequestParam("loginId")
            @NotBlank(message = "로그인 아이디를 입력해주세요.")
            String loginId
    ) {
        AuthResponse.DuplicateCheck response = authService.checkLoginId(loginId);
        return ApiResponse.success(response);
    }

    @ResponseBody
    @GetMapping("/nickname/check")
    public ApiResponse<AuthResponse.DuplicateCheck> checkNickname(
            @RequestParam("nickname")
            @NotBlank(message = "닉네임을 입력해주세요.")
            String nickname
    ) {
        AuthResponse.DuplicateCheck response = authService.checkNickname(nickname);
        return ApiResponse.success(response);
    }

    @ResponseBody
    @PostMapping("/signup")
    public ApiResponse<AuthResponse.SignupResult> signup(
            @Valid @RequestBody AuthRequest.Signup request
    ) {
        AuthResponse.SignupResult response = authService.signup(request);
        return ApiResponse.success(response);
    }

    @ResponseBody
    @PostMapping("/login")
    public ApiResponse<AuthResponse.LoginResult> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpSession session
    ) {
        AuthResponse.LoginResult response = authService.login(request, session);
        return ApiResponse.success(response);
    }

    @ResponseBody
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpSession session
    ) {

        authService.logout(session);
        return ApiResponse.success(null);
    }
}