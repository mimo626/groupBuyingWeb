package com.example.groupbuyingweb.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ORGANIZER("주최자"),
    PARTICIPANT("참여자");

    private final String description;

}