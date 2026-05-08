package com.example.groupbuyingweb.domain.dto.request;

public class AuthRequest {

    public record Signup(
            String loginId,
            String password,
            String passwordConfirm,
            String nickname,
            Boolean termsAgreed,
            String address,
            Double latitude,
            Double longitude
    ) {}

    public record Login(
            String loginId,
            String password
    ) {}
}
