package com.example.groupbuyingweb.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트 메시지"),
    SYSTEM("시스템 메시지");

    private final String description;
}
