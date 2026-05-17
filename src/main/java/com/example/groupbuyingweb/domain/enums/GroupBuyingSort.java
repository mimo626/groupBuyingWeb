package com.example.groupbuyingweb.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum GroupBuyingSort {

    LATEST("최신순", "createdAt,desc"),
    DEADLINE("마감 임박순", "deadline,asc"),
    POPULAR("인기순", "viewCount,desc");

    private final String description;
    private final String sortValue; // 실제 URL 파라미터로 넘어갈 Pageable 전용 값
}