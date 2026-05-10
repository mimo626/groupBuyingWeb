package com.example.groupbuyingweb.domain.dto.request;

import com.example.groupbuyingweb.domain.enums.GroupBuyingCategory;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class GroupBuyingRequest {

    // 공구 글 개설할 때
    public record Create(
            @NotBlank(message = "제목은 필수입니다.")
            String title,
            @NotBlank(message = "상품명은 필수입니다.")
            String productName,
            @NotNull(message = "카테고리를 선택해주세요.")
            GroupBuyingCategory category,
            String productContent,

            @Positive(message = "가격은 0보다 커야 합니다.")
            @NotNull(message = "가격을 입력해주세요.")
            Double totalPrice,

            @Positive(message = "모집 수량은 1개 이상이어야 합니다.")
            @NotNull(message = "모집 수량을 입력해주세요.")
            Integer targetQuantity,

            @Positive(message = "신청 수량은 1개 이상이어야 합니다.")
            @NotNull(message = "신청 수량을 입력해주세요.")
            Integer organizerQuantity,

            @NotNull(message = "만남 장소의 위치 정보가 필요합니다.")
            Double entX,
            @NotNull(message = "만남 장소의 위치 정보가 필요합니다.")
            Double entY,

            @NotBlank(message = "만남 장소에 대한 설명을 입력해주세요. ex) \"강남역 1번 출구 앞\"")
            String meetingPlace,

            @NotBlank(message = "상품 URL은 필수입니다.")
            String productUrl,
            String productImageUrl,
            @NotNull(message = "마감 기한을 설정해주세요.")
            LocalDateTime deadline
    ) {}

    // 공구 글 수정할 때
    public record Update(
            String title,
            String productName,
            GroupBuyingCategory category,
            String productContent,

            Double totalPrice,
            Integer targetQuantity,

            Double entX,
            Double entY,
            String meetingPlace,

            String productUrl,
            String productImageUrl,
            LocalDateTime deadline
    ) {}

    // 공구 진행 상태 변결할 때
    public record UpdateStatus(
            @NotNull(message = "변경할 상태값을 필수로 입력해야 합니다.")
            GroupBuyingStatus status,

            String trackingNumber,
            LocalDateTime meetingAt
    ) {}

    // 공구 목록 검색 조건 변경할 때
    public record SearchCondition(
            GroupBuyingCategory category, // 값이 없으면(null) 전체 조회
            String keyword     // 값이 없으면(null) 단순 목록 조회
    ) {}

    // 공구 참여할 때
    public record Participate(
            @NotNull(message = "신청 수량은 필수입니다.")
            @Positive(message = "최소 1개 이상 신청해야 합니다.")
            Integer applyQuantity
    ) {}
}