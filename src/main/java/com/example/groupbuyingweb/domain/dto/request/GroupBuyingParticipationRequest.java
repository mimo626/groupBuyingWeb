package com.example.groupbuyingweb.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GroupBuyingParticipationRequest {

    public record Charge(
            @NotBlank(message = "로그인 상태를 확인하세요")
            String memberId,
            @Positive(message = "시스템 포인트 잔여는 0보다 큽니다.")
            Double paidPoint
    ) {
    }

    public record Send( // settlement, pay
            @NotBlank(message = "로그인 상태를 확인하세요")
            String memberId,
            @NotNull(message = "공구 아이디는 필수입니다.")
            Long groupBuyingId
    ) {
    }

    public record Refund(
            @NotBlank(message = "로그인 상태를 확인하세요")
            String memberId,
            @NotNull(message = "공구 아이디는 필수입니다.")
            Long groupBuyingId,
            @Positive(message = "시스템 포인트 잔여는 0보다 큽니다.")
            Double paidPoint
    ) {
    }
}
