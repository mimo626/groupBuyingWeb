package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import com.example.groupbuyingweb.repository.h2.UserNearbyAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final UserNearbyAddressRepository userNearbyAddressRepository;

    private final AddressService addressService;
    private final GroupBuyingRepository groupBuyingRepository;
    private final GroupBuyingParticipationRepository participationRepository;

    public MyPageResponse.Profile getProfile(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow();
        int acornLevel = Math.min(20, (member.getAcornExp() / 10) + 1);

        return new MyPageResponse.Profile(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getPoint(),
                member.getAcornExp(),
                acornLevel
                );
    }

    public MyPageResponse.Neighborhood getNeighborhood(String memberId) {

        MemberRepository.MemberLocationInfo location = memberRepository.findLocationById(memberId)
                .orElseThrow();
        List<UserNearbyAddress> nearbyAddressList = userNearbyAddressRepository.findAllByMemberId(memberId);

        List<MyPageResponse.NearbyAddress> nearbyAddressDto = nearbyAddressList.stream()
                .map(entity -> new MyPageResponse.NearbyAddress(
                        entity.getCityName(),
                        entity.getDistrictName(),
                        entity.getNeighborhoodName(),
                        entity.getEntX(),
                        entity.getEntY()
                ))
                .toList();

        return new MyPageResponse.Neighborhood(
                location.getAddress(),
                location.getRadius(),
                location.getEntX(),
                location.getEntX(),
                nearbyAddressDto);
    }


    @Transactional
    public MyPageResponse.Neighborhood patchNeighborhood(String memberId, MyPageRequest.UpdateNeighborhood request) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.patchAddress(request);

        List<UserNearbyAddress> nearbyAddressList =
                addressService.createNearbyAddresses(
                        member,
                        request.entX(),
                        request.entY()
                );
        userNearbyAddressRepository.deleteAllByMemberId(memberId);

        userNearbyAddressRepository.saveAll(nearbyAddressList);

        // 2. 엔티티결과-> RESPONSEDTO
        List<MyPageResponse.NearbyAddress> nearbyAddressDto = nearbyAddressList.stream()
                .map(entity -> new MyPageResponse.NearbyAddress(
                        entity.getCityName(),
                        entity.getDistrictName(),
                        entity.getNeighborhoodName(),
                        entity.getEntX(),
                        entity.getEntY()
                ))
                .toList();

        return new MyPageResponse.Neighborhood(
                member.getAddress(),
                member.getRadius(),
                member.getEntX(),
                member.getEntY(),
                nearbyAddressDto
        );
    }

    public List<MyPageResponse.MyGroupBuyingListItem> getHostedGroupBuyings(
            String memberId,
            GroupBuyingStatus status
    ) {
        // 현재 로그인한 사용자가 개설한 공구 중 선택한 진행 상태의 공구만 조회한다.
        List<GroupBuying> groupBuyings =
                groupBuyingRepository.findAllByMember_IdAndStatusOrderByIdDesc(memberId, status);

        // participationId를 구하려면 현재 로그인한 사용자 ID도 함께 필요하다.
        return groupBuyings.stream()
                .map(groupBuying -> toMyGroupBuyingListItem(groupBuying, memberId))
                .toList();
    }

    public List<MyPageResponse.MyParticipationListItem> getParticipatedGroupBuyings(
            String memberId,
            GroupBuyingStatus status
    ) {
        // 현재 로그인한 사용자가 참여자로 등록된 공구 이력 중 선택한 진행 상태의 이력만 조회한다.
        List<GroupBuyingParticipation> participations =
                participationRepository.findAllByMember_IdAndRoleAndGroupBuying_StatusOrderByIdDesc(
                        memberId,
                        UserRole.PARTICIPANT,
                        status
                );

        // 조회된 GroupBuyingParticipation Entity 목록을 화면에 내려줄 Response DTO 목록으로 변환한다.
        return participations.stream()
                .map(this::toMyParticipationListItem)
                .toList();
    }

    private MyPageResponse.MyGroupBuyingListItem toMyGroupBuyingListItem(
            GroupBuying groupBuying,
            String memberId
    ) {
        // 상세페이지 이동에 필요한 참여 이력 ID를 조회한다.
        GroupBuyingParticipation participation =
                participationRepository.findByGroupBuyingIdAndMemberId(
                        groupBuying.getId(),
                        memberId
                );

        Long participationId = participation == null ? null : participation.getId();

        Double appliedPrice = calculateAppliedPrice( // 추가: totalPrice, targetQuantity, 주최자의 applyQuantity로 신청 가격 계산
                groupBuying.getTotalPrice(),
                groupBuying.getTargetQuantity(),
                participation == null ? null : participation.getApplyQuantity()
        );

        // 현재 참여 수량은 참여 이력의 신청 수량 합계로 계산한다.
        Long currentQuantity =
                participationRepository.sumApplyQuantityByGroupBuyingIdAndRole(
                        groupBuying.getId()
                );

        return new MyPageResponse.MyGroupBuyingListItem(
                groupBuying.getId(),
                participationId,
                groupBuying.getTitle(),
                appliedPrice, // 수정: groupBuying.getTotalPrice() 대신 계산된 신청 가격 전달
                groupBuying.getTargetQuantity(),
                toIntegerCurrentQuantity(currentQuantity),
                groupBuying.getStatus(),
                groupBuying.getDeadline()
        );
    }

    private MyPageResponse.MyParticipationListItem toMyParticipationListItem(
            GroupBuyingParticipation participation
    ) {
        // 참여 이력과 연결된 공구 정보를 가져온다.
        GroupBuying groupBuying = participation.getGroupBuying();

        Double appliedPrice = calculateAppliedPrice( // 추가: totalPrice, targetQuantity, 참여자의 applyQuantity로 신청 가격 계산
                groupBuying.getTotalPrice(),
                groupBuying.getTargetQuantity(),
                participation.getApplyQuantity()
        );

        return new MyPageResponse.MyParticipationListItem(
                participation.getId(),
                groupBuying.getId(),
                groupBuying.getTitle(),
                appliedPrice, // 수정: groupBuying.getTotalPrice() 대신 계산된 신청 가격 전달
                participation.getApplyQuantity(),
                participation.getPaidPoint(),
                participation.getPaymentStatus(),
                groupBuying.getStatus(),
                groupBuying.getMeetingAt(),
                groupBuying.getMeetingPlace(), // 수정: meetingAddress 대신 meetingPlace 전달
                groupBuying.getDeadline()
        );
    }

    private Double calculateAppliedPrice(
            Double totalPrice,
            Integer targetQuantity,
            Integer applyQuantity
    ) { // 추가: 신청 가격 계산 메서드
        if (totalPrice == null || targetQuantity == null || applyQuantity == null) {
            return 0.0;
        }

        if (targetQuantity == 0) {
            return 0.0;
        }

        return (totalPrice / targetQuantity) * applyQuantity;
    }

    private Integer toIntegerCurrentQuantity(Long currentQuantity) {
        if (currentQuantity == null) {
            return 0;
        }

        return Math.toIntExact(currentQuantity);
    }



    @Transactional
    public MyPageResponse.MyParticipationDetail getParticipationDetail(Long participationId) {
        GroupBuyingParticipation participation = participationRepository.findById(participationId)
                .orElseThrow();
        GroupBuying groupBuying = participation.getGroupBuying();

        int currentQuantity = participationRepository.sumQuantity(groupBuying.getId()); // 공구 현재 수량


        List<MyPageResponse.ProgressStep> steps = getProgressSteps(groupBuying);

        return new MyPageResponse.MyParticipationDetail(
                participation.getId(),
                groupBuying.getId(),
                groupBuying.getTitle(),
                groupBuying.getProductName(),
                groupBuying.getProductContent(),
                groupBuying.getTotalPrice(),
                groupBuying.getTargetQuantity(),
                currentQuantity,
                participation.getApplyQuantity(),
                participation.getPaidPoint(),
                participation.getPaymentStatus(),
                groupBuying.getStatus(),
                groupBuying.getTrackingNumber(),
                groupBuying.getMeetingAddress(),
                groupBuying.getMeetingAt(),
                steps
        );
    }

    private static List<MyPageResponse.ProgressStep> getProgressSteps(GroupBuying groupBuying) {
        List<MyPageResponse.ProgressStep> steps = new ArrayList<>();
        GroupBuyingStatus[] allStatuses = GroupBuyingStatus.values();

        int currentOrdinal = groupBuying.getStatus().ordinal();

        for (GroupBuyingStatus status : allStatuses) {
            // 1. 완료 여부
            boolean isCompleted = status.ordinal() < currentOrdinal;
            // 2. 활성 여부
            boolean isActive = status.ordinal() == currentOrdinal;

            steps.add(new MyPageResponse.ProgressStep(
                    status.getDescription(),
                    isCompleted,
                    isActive
            ));
        }
        return steps;
    }
}
