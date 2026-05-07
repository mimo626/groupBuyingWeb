package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final GroupBuyingParticipationRepository participationRepo;
    private final MemberRepository memberRepository;

    @Transactional
    public GroupBuyingParticipationResponse.UserResult payPoint(GroupBuyingParticipationRequest.Send request) {
        // 1. 공구 참여 정보 조회
        GroupBuyingParticipation participation = participationRepo.findById(request.groupBuyingId())
                .orElseThrow();
        // 2. 회원 정보 조회
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow();

        // 3. 가격 게산
        GroupBuying gb = participation.getGroupBuying();
        double totalPay = gb.getTotalPrice() / gb.getTargetQuantity() * participation.getApplyQuantity();

        // 4. 로직 수행
        memberRepository.decreasePoint(request.memberId(), totalPay);
        participationRepo.increasePaidPoint(request.groupBuyingId(), totalPay);

        return new GroupBuyingParticipationResponse.UserResult(member.getId());

    }
}
