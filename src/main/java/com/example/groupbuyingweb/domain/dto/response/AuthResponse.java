package com.example.groupbuyingweb.domain.dto.response;

public class AuthResponse {

    public record SignupResult(
            String userId
    ) {}

    public record LoginResult(
            String userId,
            String loginId,
            String nickname,
            String address,
            Integer radius,
            Double latitude,
            Double longitude
    ) {}

    public record DuplicateCheck(
            Boolean available
    ) {}

    public record Me(
            String userId,
            String loginId,
            String nickname
    ) {}
}
