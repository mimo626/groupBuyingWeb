package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.groupbuyingweb.domain.enums.UserRole.ORGANIZER;

@Service
@RequiredArgsConstructor
public class PointService {

    private final GroupBuyingParticipationRepository participationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void payPoint(String memberId, long groupBuyingId, Double totalPrice, Integer targetQuantity) {
        // 1. 공구 참여 정보 조회
        GroupBuyingParticipation participation = participationRepository.findByGroupBuyingIdAndMemberId(groupBuyingId, memberId);

        // 2. 회원 정보 조회
        Member member = memberRepository.findById(memberId).orElseThrow();
        double pricePerQuantity = totalPrice / targetQuantity;

        // 3. 가격 계산
        double totalPay = pricePerQuantity * participation.getApplyQuantity();

        // 4. 로직 수행
        member.decreasePoint(totalPay);
        participation.increasePoint(totalPay);
    }

    @Transactional
    public GroupBuyingParticipationResponse.UserResult chargePoint(String memberId, Double charge) {
        // 1. 회원 정보 조회
        Member member = memberRepository.findById(memberId).orElseThrow();

        // 2. 충전 로직 수행
        member.chargePoint(charge);
        return new GroupBuyingParticipationResponse.UserResult(memberId);
    }

    @Transactional
    public void settlePoint(long groupBuyingId){

        // 1. 공구에 속하는 참여 엔티티의 paidPoint 합산
        double settlePay = participationRepository.sumSettlePay(groupBuyingId);

        // 2. 해당 공구의 공구 참여 엔티티 리스트 조회
        List<GroupBuyingParticipation> system = participationRepository.findAllByGroupBuyingId(groupBuyingId);

        // 3. 시스템에 보관된 포인트 차감
        for (GroupBuyingParticipation participation : system){
            participation.settlePoint(participation.getPaidPoint()); // 시스템 포인트 잔여 0
        }
        // 3. 주최자 조회 후 포인트 정산
        Member organizer = participationRepository.findMemberByGroupBuyingIdAndRole(groupBuyingId, ORGANIZER);
        organizer.chargePoint(settlePay);
    }
}
