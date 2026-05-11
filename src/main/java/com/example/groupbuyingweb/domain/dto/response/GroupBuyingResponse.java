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
    ) {
        // Entity -> DTO 변환 정적 팩토리 메서드
        public static List of(GroupBuying groupBuying, int currentQuantity) {
            return new List(
                    groupBuying.getId(),
                    groupBuying.getTitle(),
                    groupBuying.getProductImageUrl(),
                    groupBuying.getTargetQuantity() > 0 ? groupBuying.getTotalPrice() / groupBuying.getTargetQuantity() : 0,
                    groupBuying.getTargetQuantity(),
                    currentQuantity,
                    groupBuying.getNeighborhoodName(),
                    groupBuying.getViewCount(),
                    groupBuying.getStatus(),
                    groupBuying.getDeadline(),
                    groupBuying.getCreatedAt()
            );
        }
    }

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
        public static Detail of(GroupBuying groupBuying, int currentQuantity) {
            return new Detail(
                    groupBuying.getId(),
                    groupBuying.getTitle(),
                    groupBuying.getProductImageUrl(),
                    groupBuying.getProductUrl(),
                    groupBuying.getTotalPrice(),
                    groupBuying.getTargetQuantity() > 0 ? groupBuying.getTotalPrice() / groupBuying.getTargetQuantity() : 0,
                    groupBuying.getTargetQuantity(),
                    currentQuantity,
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
            Long groupBuyingId,
            GroupBuyingStatus status
    ) {
        public static UpdateStatus from(GroupBuying groupBuying) {
            return new UpdateStatus(
                    groupBuying.getId(),
                    groupBuying.getStatus()
            );
        }
    }
}
