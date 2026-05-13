package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.groupbuyingweb.domain.enums.UserRole.ORGANIZER;

@Service
@RequiredArgsConstructor
public class PointService {

    private final GroupBuyingParticipationRepository participationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void payPoint(Member member, GroupBuyingParticipation participation, double unitPrice) {
        // 0. 결제 유저가 주최자라면 지불 x
        if (participation.getGroupBuying().getMember().getId().equals(member.getId())) return;
        // 1. 총 결제 금액 계산 = 단가 * 내 신청 수량
        double totalPay = unitPrice * participation.getApplyQuantity();

        // 2. 포인트 차감 및 증가 (잔액 부족 예외 처리 로직이 내부에 있다고 가정합니다)
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
    public GroupBuyingParticipationResponse.SettleResult settlePoint(long groupBuyingId, String memberId){

        // 1. 공구에 속하는 참여 엔티티의 paidPoint 정산
        GroupBuyingParticipation participation = participationRepository.findByGroupBuyingIdAndMemberId(groupBuyingId, memberId);
        Double point = participation.getPaidPoint();
        participation.settlePoint(point);

        // 2. 주최자 찾아서 정산
        Member organizer = participationRepository.findMemberByGroupBuyingIdAndRole(groupBuyingId, ORGANIZER);
        organizer.chargePoint(point);

        // 3. 해당 공구의 모든 공구 참여 엔티티의 PaymentStatus 가 Complete 라면 true, Incomplete 하나라도 있으면 false
        boolean hasIncomplete = participationRepository.existsByGroupBuyingIdAndPaymentStatus(groupBuyingId, PaymentStatus.Incomplete);

        return new GroupBuyingParticipationResponse.SettleResult(
                groupBuyingId,
                !hasIncomplete
        );
    }

// 폐기된 전체 정산 로직
//    @Transactional
//    public void settlePoint(long groupBuyingId){
//
//        // 1. 공구에 속하는 참여 엔티티의 paidPoint 합산
//        double settlePay = participationRepository.sumSettlePay(groupBuyingId);
//
//        // 2. 해당 공구의 공구 참여 엔티티 리스트 조회
//        List<GroupBuyingParticipation> system = participationRepository.findAllByGroupBuyingId(groupBuyingId);
//
//        // 3. 시스템에 보관된 포인트 차감
//        for (GroupBuyingParticipation participation : system){
//            participation.settlePoint(participation.getPaidPoint()); // 시스템 포인트 잔여 0
//        }
//        // 3. 주최자 조회 후 포인트 정산
//        Member organizer = participationRepository.findMemberByGroupBuyingIdAndRole(groupBuyingId, ORGANIZER);
//        organizer.chargePoint(settlePay);
//    }
}
