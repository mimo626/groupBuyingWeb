package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final GroupBuyingParticipationRepository participationRepository;
    private final MemberRepository memberRepository;
    private final GroupBuyingRepository groupBuyingRepository;

    @Transactional
    public void payPoint(long groupBuyingId, Double totalPrice, Integer targetQuantity) {
        // 1. 공구 참여 정보 조회
        GroupBuyingParticipation participation = participationRepository.findById(groupBuyingId).orElseThrow();

        // 2. 회원 정보 조회
        Member member = participation.getMember();
        double pricePerQuantity = totalPrice / targetQuantity;

        // 3. 가격 계산
        double totalPay = pricePerQuantity * participation.getApplyQuantity();

        // 4. 로직 수행
        member.decreasePoint(totalPay);
        participation.increasePoint(totalPay);
    }

    public GroupBuyingParticipationResponse.UserResult chargePoint(String memberId, Double charge) {
        // 1. 회원 정보 조회
        Member member = memberRepository.findById(memberId).orElseThrow();
        // 2. 충전 로직 수행
        member.chargePoint(charge);
        return new GroupBuyingParticipationResponse.UserResult(memberId);
    }

//    @Transactional
//    public GroupBuyingParticipationResponse.UserResult refundPoint(GroupBuyingParticipationRequest.Send request) {
//        // 1. 공구 참여 정보 조회
//        GroupBuyingParticipation participation = participationRepo.findById(request.groupBuyingId())
//                .orElseThrow();
//        // 2. 회원 정보 조회
//        Member member = memberRepository.findById(request.memberId())
//                .orElseThrow();
//
//        // 3. 환불할 paidPoint 조회
//        double refundPay = participation.getPaidPoint();
//
//        // 4. 로직 수행
//        participationRepo.decreasePaidPoint(request.groupBuyingId(), refundPay);
//        memberRepository.increasePoint(request.memberId(), refundPay);
//
//        return new GroupBuyingParticipationResponse.UserResult(member.getId());
//    }
}
