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
            String content,

            @Positive(message = "가격은 0보다 커야 합니다.")
            double totalPrice,
            @Positive(message = "모집 인원은 1명 이상이어야 합니다.")
            int targetQuantity,

            // 지도 관련 정보
            @NotNull(message = "만남 장소의 위치 정보가 필요합니다.")
            double latitude,
            @NotNull(message = "만남 장소의 위치 정보가 필요합니다.")
            double longitude,
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

            Double latitude,
            Double longitude,
            String meetingAddress,

            String productUrl,
            String productImageUrl,
            LocalDateTime deadline
    ) {}

    // 공구 진행 상태 변결할 때
    public record UpdateStatus(
            @NotNull(message = "변경할 상태값을 필수로 입력해야 합니다.")
            GroupBuyingStatus status,

            String trackingNumber,
            LocalDateTime meetingDate
    ) {}

    // 공구 목록 검색 조건 변경할 때
    public record SearchCondition(
            GroupBuyingCategory category, // 값이 없으면(null) 전체 조회
            String keyword     // 값이 없으면(null) 단순 목록 조회
    ) {}
}