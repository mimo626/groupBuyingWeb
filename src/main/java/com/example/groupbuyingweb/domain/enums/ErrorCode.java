package com.example.groupbuyingweb.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(500,"서버 내부 오류입니다."),
    // 포인트
    INSUFFICIENT_POINT(400, "포인트 잔액이 부족합니다."),

    // test 용 - 충전 포인트 0 이면 반환해볼예정
    TEST_ZERO_POINT(400, "포인트는 0이 못된당");

    private final int status;
    private final String message;
}
