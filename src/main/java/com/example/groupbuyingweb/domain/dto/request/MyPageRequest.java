package com.example.groupbuyingweb.domain.dto.request;

public class MyPageRequest {

    public record UpdateNeighborhood(
            String address,
            Double latitude,
            Double longitude
    ) {}

    public record MyGroupBuyingSearchCondition(
            String status
    ) {}

    public record UpdateMyGroupBuying(
            String title,
            String content,
            String thumbnailUrl,
            Integer price,
            Integer targetCount,
            String pickupLocation,
            String deadlineAt
    ) {}

    public record ParticipationSearchCondition(
            String status
    ) {}
}
