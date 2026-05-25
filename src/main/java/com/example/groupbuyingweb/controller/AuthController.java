package com.example.groupbuyingweb.controller;

import com.example.groupbuyingweb.core.api.ApiResponse;
import com.example.groupbuyingweb.domain.dto.request.AuthRequest;
import com.example.groupbuyingweb.domain.dto.response.AuthResponse;
import com.example.groupbuyingweb.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // 성공 시 200 OK
    // 실패(로그인 아이디 미입력 또는 유효성 에러) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/login-ids/check")
    public ApiResponse<AuthResponse.DuplicateCheck> checkLoginId(
            @RequestParam("loginId")
            @NotBlank(message = "로그인 아이디를 입력해주세요.")
            @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력해주세요.")
            @Pattern(regexp = "^[a-z0-9]+$", message = "로그인 아이디는 영문 소문자와 숫자만 사용할 수 있습니다.")
            String loginId
    ) {
        AuthResponse.DuplicateCheck response = authService.checkLoginId(loginId);
        return ApiResponse.success(response);
    }

    // 성공 시 200 OK
    // 실패(닉네임 미입력 또는 유효성 에러) 시 400 Bad Request
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @GetMapping("/nicknames/check")
    public ApiResponse<AuthResponse.DuplicateCheck> checkNickname(
            @RequestParam("nickname")
            @NotBlank(message = "닉네임을 입력해주세요.")
            @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            String nickname
    ) {
        AuthResponse.DuplicateCheck response = authService.checkNickname(nickname);
        return ApiResponse.success(response);
    }


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
            HttpSession session,
            HttpServletRequest httpRequest, // 추가: SecurityContext를 세션에 저장하기 위해 Service로 전달한다.
            HttpServletResponse httpResponse // 추가: SecurityContext를 세션에 저장하기 위해 Service로 전달한다.
    ) {
        AuthResponse.LoginResult response = authService.login(request, session, httpRequest, httpResponse); // 수정: 변경된 AuthService.login() 시그니처에 맞춰 요청/응답 객체를 함께 전달한다.
        return ApiResponse.success(response);
    }


    // 추가: 성공/실패 상태 코드 기준 주석 정리
    // 성공 시 200 OK
    // 실패(인증되지 않은 사용자) 시 401 Unauthorized
    // 실패(서버 내부 오류) 시 500 Internal Server Error
    @ResponseBody
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpSession session,
            HttpServletRequest httpRequest, // 추가: SecurityContext 정리를 위해 Service로 전달한다.
            HttpServletResponse httpResponse // 추가: SecurityContext 정리를 위해 Service로 전달한다.
    ) {
        authService.logout(session, httpRequest, httpResponse); // 수정: 변경된 AuthService.logout() 시그니처에 맞춰 요청/응답 객체를 함께 전달한다.
        return ApiResponse.success(null);
    }
}