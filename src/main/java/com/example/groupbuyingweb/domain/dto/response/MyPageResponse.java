package com.example.groupbuyingweb.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class MyPageResponse {

    public record Profile(
            String userId,
            String loginId,
            String nickname,
            Integer point,
            Integer acornExp,
            Integer acornLevel,
            LocalDateTime createdAt
    ) {}

    public record Neighborhood(
            String address,
            Integer radius,
            Double latitude,
            Double longitude,
            List<NearbyAddress> nearbyAddresses
    ) {}

    public record NearbyAddress(
            String cityName,
            String districtName,
            String neighborhoodName,
            Double latitude,
            Double longitude
    ) {}

    public record MyGroupBuyingListItem(
            Long groupBuyId,
            String title,
            String thumbnailUrl,
            Integer price,
            Integer targetCount,
            Integer currentCount,
            String status,
            String deadlineAt,
            LocalDateTime createdAt
    ) {}

    public record MyGroupBuyingDetail(
            Long groupBuyId,
            String title,
            String content,
            String thumbnailUrl,
            Integer price,
            Integer targetCount,
            Integer currentCount,
            String pickupLocation,
            String deadlineAt,
            String status,
            LocalDateTime createdAt
    ) {}

    public record UpdateMyGroupBuyingResult(
            Long groupBuyId,
            String title,
            String content,
            String thumbnailUrl,
            Integer price,
            Integer targetCount,
            String pickupLocation,
            String deadlineAt,
            String status,
            LocalDateTime updatedAt
    ) {}

    public record MyParticipationListItem(
            Long participationId,
            Long groupBuyId,
            String title,
            String thumbnailUrl,
            Integer price,
            Integer quantity,
            String participationStatus
    ) {}

    public record MyParticipationDetail(
            Long participationId,
            Long groupBuyId,
            String title,
            String content,
            Integer price,
            Integer quantity,
            Integer targetCount,
            Integer currentCount,
            String pickupLocation,
            String deadlineAt,
            String participationStatus,
            String groupBuyStatus,
            String joinedAt
    ) {}
}
