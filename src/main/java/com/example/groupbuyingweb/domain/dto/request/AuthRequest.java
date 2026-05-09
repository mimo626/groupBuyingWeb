package com.example.groupbuyingweb.domain.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthRequest {

    public record Signup(
            @NotBlank(message = "로그인 아이디를 입력해주세요.")
            String loginId,

            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password,

            @NotBlank(message = "비밀번호 확인값을 입력해주세요.")
            String passwordConfirm,

            @NotBlank(message = "닉네임을 입력해주세요.")
            String nickname,

            @NotNull(message = "필수 약관 동의 여부는 필수입니다.")
            @AssertTrue(message = "필수 약관에 동의해야 합니다.")
            Boolean termsAgreed,

            @NotBlank(message = "전체 주소를 입력해주세요.")
            String address,

            @NotNull(message = "기준 주소 X좌표는 필수입니다.")
            Double entX,

            @NotNull(message = "기준 주소 Y좌표는 필수입니다.")
            Double entY
    ) {}

    public record Login(
            @NotBlank(message = "로그인 아이디를 입력해주세요.")
            String loginId,

            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password
    ) {}
}