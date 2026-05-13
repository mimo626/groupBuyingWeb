package com.example.groupbuyingweb.domain.dto.request;

import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MyPageRequest {

    public record UpdateNeighborhood(
            @NotBlank(message = "전체 주소를 입력해주세요.")
            String address,

            @NotNull(message = "기준 주소 X좌표는 필수입니다.")
            Double entX,

            @NotNull(message = "기준 주소 Y좌표는 필수입니다.")
            Double entY
    ) {}

    public record MyGroupBuyingSearchCondition(
            @NotNull(message = "공구 진행 상태는 필수입니다.")
            GroupBuyingStatus status
    ) {}

    public record ParticipationId(
        Long participationId
    ) {}
}