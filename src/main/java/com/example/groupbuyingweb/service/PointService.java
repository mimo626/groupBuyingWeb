package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final GroupBuyingParticipationRepository participationRepo;
    private final MemberRepository memberRepository;
    private final GroupBuyingRepository groupBuyingRepo;

    @Transactional
    public void payPoint(long groupBuyingId, Double totalPrice, Integer targetQuantity) {
        // 1. 공구 참여 정보 조회
        List<GroupBuyingParticipation> participationList = Collections.singletonList(participationRepo.findById(groupBuyingId)
                .orElseThrow());


        double pricePerQantity = totalPrice / targetQuantity;

        for (GroupBuyingParticipation participation : participationList){
            // 3. 가격 게산
            double totalPay = pricePerQantity * participation.getApplyQuantity();
            // 2. 회원 정보 조회
            Member member = participation.getMember();
            // 4. 로직 수행
            member.decreasePoint(totalPay);
            participation.increasePoint(totalPay);
        }
    }

    @Transactional
    public GroupBuyingParticipationResponse.UserResult refundPoint(GroupBuyingParticipationRequest.Send request) {
        // 1. 공구 참여 정보 조회
        GroupBuyingParticipation participation = participationRepo.findById(request.groupBuyingId())
                .orElseThrow();
        // 2. 회원 정보 조회
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow();

        // 3. 환불할 paidPoint 조회
        double refundPay = participation.getPaidPoint();

        // 4. 로직 수행
        participationRepo.decreasePaidPoint(request.groupBuyingId(), refundPay);
        memberRepository.increasePoint(request.memberId(), refundPay);

        return new GroupBuyingParticipationResponse.UserResult(member.getId());
    }
}
