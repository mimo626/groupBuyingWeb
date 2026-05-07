package com.example.groupbuyingweb.domain.dto.request;

public class GroupBuyingParticipationRequest {

    public record Charge(
            int userId,
            Long paidPoint
    ) {
    }

    public record Send( // settlement, pay
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
