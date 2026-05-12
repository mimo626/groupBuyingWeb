package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import com.example.groupbuyingweb.repository.UserNearbyAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
        userNearbyAddressRepository.deleteAllByMemberId(memberId);


        // 변경된
        List<UserNearbyAddress> nearbyAddressList =
                addressService.createNearbyAddresses(
                        member,
                        request.entX(),
                        request.entY()
                );

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

    public List<MyPageResponse.MyGroupBuyingListItem> getHostedGroupBuyings(String memberId, GroupBuyingStatus status) {
        // 1. 리포지토리 호출 (status가 null이면 전체, 값이 있으면 필터링해서 엔티티를 가져옴)
        List<MyPageResponse.MyGroupBuyingListItem> dtoList = null;//groupBuyingRepository.findMyGroupBuyings(memberId, status);

        // 2. 엔티티 리스트를 DTO 리스트로 변환하여 반환
        return dtoList;
    }









    @Transactional
    public MyPageResponse.MyParticipationDetail getParticipationDetail(String memberId, Long participationId) {
        GroupBuyingParticipation participation = participationRepository.findById(participationId)
                .orElseThrow();
        GroupBuying groupBuying = participation.getGroupBuying();
        MyPageResponse.MyParticipationDetail dto = null;

        int CurrentQuantity = participationRepository.sumQuantity(groupBuying.getId()); // 공구 현재 수량


        List<MyPageResponse.ProgressStep> steps = getProgressSteps(groupBuying);

        return new MyPageResponse.MyParticipationDetail(
                participation.getId(),
                groupBuying.getId(),
                groupBuying.getTitle(),
                groupBuying.getProductName(),
                groupBuying.getProductContent(),
                groupBuying.getTotalPrice(),
                groupBuying.getTargetQuantity(),
                CurrentQuantity,
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
