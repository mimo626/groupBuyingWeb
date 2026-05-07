package com.example.groupbuyingweb.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupBuyingStatus {

    RECRUITING("모집 중"),
    START("공구 시작"),
    PURCHASED("물품 구매 완료"),
    SHIPPING("배송 중"),
    MEETING_SCHEDULED("만남 날짜 입력 완료"),
    SETTLING("정산 진행 중"),
    CLOSED("공구 종료");

    private final String description;
}