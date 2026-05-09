package com.example.groupbuyingweb.domain.dto.response;

import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class MyPageResponse {

    public record Profile(
            String userId,
            String loginId,
            String nickname,
            Double point,
            Integer acornExp,
            Integer acornLevel
    ) {}

    public record Neighborhood(
            String address,
            Integer radius,
            Double entX,
            Double entY,
            List<NearbyAddress> nearbyAddresses
    ) {}

    public record NearbyAddress(
            String cityName,
            String districtName,
            String neighborhoodName,
            Double entX,
            Double entY
    ) {}

    public record MyGroupBuyingListItem(
            Long groupBuyingId,
            String title,
            String productName,
            String productImageUrl,
            Double totalPrice,
            Integer targetQuantity,
            Integer currentQuantity,
            String neighborhoodName,
            Integer viewCount,
            GroupBuyingStatus status,
            LocalDateTime deadline,
            LocalDateTime createdAt
    ) {}

    public record MyParticipationListItem(
            Long participationId,
            Long groupBuyingId,
            String title,
            String productName,
            String productImageUrl,
            Integer applyQuantity,
            Double paidPoint,
            PaymentStatus paymentStatus,
            GroupBuyingStatus groupBuyingStatus,
            LocalDateTime deadline
    ) {}

    public record MyParticipationDetail(
            Long participationId,
            Long groupBuyingId,
            String title,
            String productName,
            String productImageUrl,
            String productUrl,
            String productContent,
            Double totalPrice,
            Integer targetQuantity,
            Integer currentQuantity,
            Integer applyQuantity,
            Double paidPoint,
            PaymentStatus paymentStatus,
            GroupBuyingStatus groupBuyingStatus,
            String trackingNumber,
            String meetingAddress,
            LocalDateTime meetingAt,
            LocalDateTime deadline,
            List<ProgressStep> progressSteps
    ) {}

    public record ProgressStep(
            String label,
            Boolean completed,
            Boolean active
    ) {}
}