package com.example.groupbuyingweb.domain.dto.response;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;

import java.time.LocalDateTime;

public class GroupBuyingResponse {

    // 공구 목록 조회
    public record List(
            Long id,
            String title,
            String productImageUrl,
            double unitPrice,
            int targetQuantity,
            int currentQuantity,
            String neighborhoodName, // 동네 이름 --동
            int viewCount,
            GroupBuyingStatus status,
            LocalDateTime deadline,
            LocalDateTime createAt
    ) {}

    // 공구 상세 조회
    public record Detail(
            Long id,
            String title,
            String productImageUrl,
            String productUrl,
            double totalPrice,
            double unitPrice,
            int targetQuantity,
            int currentQuantity,
            String meetingPlace, // 만남 장소 텍스트
            int viewCount,
            GroupBuyingStatus status,
            String content,
            LocalDateTime deadline,
            LocalDateTime createAt
    ) {
        public Detail(GroupBuying groupBuying) {
            this(
                    groupBuying.getId(),
                    groupBuying.getTitle(),
                    groupBuying.getProductImageUrl(),
                    groupBuying.getProductUrl(),
                    groupBuying.getTotalPrice(),
                    // 1인당 가격 계산 (0으로 나누기 방지)
                    groupBuying.getTargetQuantity() > 0 ? groupBuying.getTotalPrice() / groupBuying.getTargetQuantity() : 0,
                    groupBuying.getTargetQuantity(),
                    0,
                    groupBuying.getMeetingPlace(),
                    groupBuying.getViewCount(),
                    groupBuying.getStatus(),
                    groupBuying.getProductContent(),
                    groupBuying.getDeadline(),
                    groupBuying.getCreatedAt()
            );
        }
    }

    public record Create(
            Long groupBuyingId,
            Long groupBuyingPartiId
    ) {}

    public record Participate(
            Long groupBuyingId,
            Long groupBuyingPartiId
    ) {}

    public record UpdateStatus(
            Long groupBuyingId
    ) {}
}
