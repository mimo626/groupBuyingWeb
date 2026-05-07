package com.example.groupbuyingweb.domain.dto.response;

public class GroupBuyingParticipationResponse {

    // 포인트 이동 시 결과값 : 사용자id
    public record UserResult(String memberId){}

    // 정산 시 결과값 : 정산한 공구id
    public record SettleResult(Long groupBuyingId){}
}
