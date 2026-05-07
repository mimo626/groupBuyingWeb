package com.example.groupbuyingweb.domain.dto.response;

import java.time.LocalDateTime;

public class GroupBuyingResponse {

    // 1. 목록 조회용
    public record List(
            Long id,
            String title,
            String productImageUrl,
            int unitPrice,
            int targetQuantity,
            int currentQuantity,
            String neighborhoodName,
            int viewCount,
            String status,
            LocalDateTime deadline,
            LocalDateTime createDate
    ) {}

    // 2. 상세 조회용
    public record Detail(
            Long id,
            String title,
            String productImageUrl,
            String productUrl,
            double totalPrice,
            int unitPrice,
            int targetQuantity,
            int currentQuantity,
            String neighborhoodName,
            int viewCount,
            String status,
            String content,
            LocalDateTime deadline,
            LocalDateTime createDate
    ) {}
}
