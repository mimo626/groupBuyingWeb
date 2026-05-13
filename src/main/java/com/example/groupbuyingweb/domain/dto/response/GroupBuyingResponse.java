package com.example.groupbuyingweb.domain.dto.response;

import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingImage;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;

import java.time.LocalDateTime;
import java.util.List;

public class GroupBuyingResponse {

    // 공구 목록 조회
    public record GroupBuyings(
            Long id,
            String title,
            String productName,
            double unitPrice,
            int targetQuantity,
            int currentQuantity,
            String neighborhoodName, // 동네 이름 --동
            int viewCount,
            GroupBuyingStatus status,
            String thumbnailImage,
            String dDayString,
            LocalDateTime deadline,
            LocalDateTime createAt
    ) {
        // Entity -> DTO 변환 정적 팩토리 메서드
        public static GroupBuyings of(GroupBuying groupBuying, String thumbnailImage, int currentQuantity, String dDayString) {
            return new GroupBuyings(
                    groupBuying.getId(),
                    groupBuying.getTitle(),
                    groupBuying.getProductName(),
                    groupBuying.getTargetQuantity() > 0 ? groupBuying.getTotalPrice() / groupBuying.getTargetQuantity() : 0,
                    groupBuying.getTargetQuantity(),
                    currentQuantity,
                    groupBuying.getNeighborhoodName(),
                    groupBuying.getViewCount(),
                    groupBuying.getStatus(),
                    thumbnailImage,
                    dDayString,
                    groupBuying.getDeadline(),
                    groupBuying.getCreatedAt()
            );
        }
    }

    // 공구 상세 조회
    public record Detail(
            Long id,
            MemberInfo member, // 엔티티(Member) 대신 DTO(MemberInfo)로 변경
            String title,
            String productName,
            String productContent,
            String productUrl,
            double totalPrice,
            double unitPrice,
            int targetQuantity,
            int currentQuantity,
            String meetingPlace, // 만남 장소 텍스트
            String meetingAddress,
            int viewCount,
            GroupBuyingStatus status,
            LocalDateTime deadline,
            List<ImageDetail> images,
            LocalDateTime createAt,
            boolean isOrganizer,     // 현재 로그인한 사용자가 주최자인지 여부
            boolean isParticipant,   // 현재 로그인한 사용자가 참여자인지 여부
            boolean hasParticipants  // 현재 이 공구에 참여자가 1명이라도 있는지 (수정/삭제 제한용)
    ) {
        public record MemberInfo(String memberId, String nickname) {
            // 엔티티를 받아서 DTO로 변환해주는 메서드
            public static MemberInfo from(Member member) {
                return new MemberInfo(member.getId(), member.getNickname());
            }
        }

        public static Detail of(GroupBuying groupBuying, int currentQuantity,
                                boolean isOrganizer, boolean isParticipant, boolean hasParticipants) {
            return new Detail(
                    groupBuying.getId(),
                    MemberInfo.from(groupBuying.getMember()), // 👈 엔티티를 DTO로 변환해서 넣음
                    groupBuying.getTitle(),
                    groupBuying.getProductName(),
                    groupBuying.getProductContent(),
                    groupBuying.getProductUrl(),
                    groupBuying.getTotalPrice(),
                    groupBuying.getTargetQuantity() > 0 ? groupBuying.getTotalPrice() / groupBuying.getTargetQuantity() : 0,
                    groupBuying.getTargetQuantity(),
                    currentQuantity,
                    groupBuying.getMeetingPlace(),
                    groupBuying.getMeetingAddress(),
                    groupBuying.getViewCount(),
                    groupBuying.getStatus(),
                    groupBuying.getDeadline(),
                    groupBuying.getImages().stream()
                            .map(ImageDetail::of)
                            .toList(),
                    groupBuying.getCreatedAt(),
                    isOrganizer,
                    isParticipant,
                    hasParticipants
            );
        }
    }

    public record ImageDetail(
            String imageUrl,
            boolean isThumbnail
    ) {
        public static ImageDetail of(GroupBuyingImage image) {
            return new ImageDetail(image.getImageUrl(), image.isThumbnail());
        }
    }

    public record Create(
            Long groupBuyingId,
            Long groupBuyingPartiId
    ) {}

    public record Participate(
            Long groupBuyingId,
            Long groupBuyingPartiId,
            Integer applyQuantity
    ) {}

    public record UpdateStatus(
            Long groupBuyingId,
            GroupBuyingStatus status
    ) {
        public static UpdateStatus from(GroupBuying groupBuying) {
            return new UpdateStatus(
                    groupBuying.getId(),
                    groupBuying.getStatus()
            );
        }
    }
}
