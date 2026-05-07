package com.example.groupbuyingweb.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupBuyingSort {

    LATEST("최신순"),      // 기본값 (생성일 내림차순)
    DEADLINE("마감 임박순"), // 마감일 오름차순 (현재 시간과 가장 가까운 순)
    POPULAR("인기순");      // 조회수 내림차순

    private final String description;
}