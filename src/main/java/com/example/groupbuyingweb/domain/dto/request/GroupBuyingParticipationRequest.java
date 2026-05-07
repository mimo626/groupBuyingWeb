package com.example.groupbuyingweb.domain.dto.request;

public class GroupBuyingParticipationRequest {

    public record Charge(
            int memberId,
            Long paidPoint
    ) {
    }

    public record Send( // settlement, pay
                        String memberId,
                        Long groupBuyingId
    ) {
    }

    public record Refund(
            int memberId,
            Long groupBuyingId,
            Long paidPoint
    ) {
    }
}
