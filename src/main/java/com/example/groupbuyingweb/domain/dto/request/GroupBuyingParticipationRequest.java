package com.example.groupbuyingweb.domain.dto.request;

public class GroupBuyingParticipationRequest {
    public record Pay(
            Long groupBuyingId
    ) {
    }

    public record Charge(
            int userId,
            Long paidPoint
    ) {
    }

    public record Settle(
            int userId,
            Long groupBuyingId
    ) {
    }

    public record Refund(
            int userId,
            Long groupBuyingId,
            Long paidPoint
    ) {
    }
}
