package com.example.groupbuyingweb.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    Incomplete("정산 미완료"),
    Complete("정산 완료");
    private final String description;
    }
