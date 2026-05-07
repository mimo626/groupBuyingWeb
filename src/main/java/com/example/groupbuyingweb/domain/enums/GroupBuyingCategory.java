package com.example.groupbuyingweb.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupBuyingCategory {

    DAILY_NECESSITY("생활용품"),
    BEAUTY("미용용품"),
    FOOD("식품"),
    CLOTHING_ACC("의류/잡화"),
    BABY("유아용품"),
    PET("반려동물용품"),
    ETC("기타");

    private final String description;
}