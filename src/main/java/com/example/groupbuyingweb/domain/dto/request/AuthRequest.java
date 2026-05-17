package com.example.groupbuyingweb.domain.dto.request;

import jakarta.validation.constraints.*;

public class AuthRequest {

        public record Signup(
                @NotBlank(message = "로그인 아이디를 입력해주세요.")
                @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력해주세요.")
                @Pattern(regexp = "^[a-z0-9]+$", message = "로그인 아이디는 영문 소문자와 숫자만 사용할 수 있습니다.")
                String loginId,

                @NotBlank(message = "비밀번호를 입력해주세요.")
                @Pattern(
                        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                        message = "비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상 20자 이하로 입력해주세요."
                )
                String password,

                @NotBlank(message = "비밀번호 확인값을 입력해주세요.")
                String passwordConfirm,

                @NotBlank(message = "닉네임을 입력해주세요.")
                @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
                @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
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
                @Size(min = 4, max = 20, message = "로그인 아이디는 4자 이상 20자 이하로 입력해주세요.")
                @Pattern(regexp = "^[a-z0-9]+$", message = "로그인 아이디는 영문 소문자와 숫자만 사용할 수 있습니다.")
                String loginId,

                @NotBlank(message = "비밀번호를 입력해주세요.")
                String password
        ) {}
}