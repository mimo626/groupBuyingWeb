package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupBuyingService {
    @Autowired
    private GroupBuyingRepository groupBuyingRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    public GroupBuyingResponse.Create addGroupBuying(GroupBuyingRequest.Create request, String memberId) {
//        공구 생성 dto로 공구 엔티티 생성(필요한 값 추가)
        // 작성자(Member) 존재 확인
        Member member = memberRepository.findAll().get(0);
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // DTO -> Entity 변환 (Builder 사용)
        GroupBuying groupBuying = GroupBuying.builder()
                .member(member)
                .title(request.title())
                .productName(request.productName())
                .category(request.category())
                .productContent(request.productContent())
                .totalPrice(request.totalPrice())
                .targetQuantity(request.targetQuantity())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .meetingPlace(request.meetingPlace())
                .productUrl(request.productUrl())
                .productImageUrl(request.productImageUrl())
                .deadline(request.deadline())
                .neighborhoodName("상봉동")
                .build();

//        공구 레포지토리의 save() 실행 → 공구 반환 시 성공
        GroupBuying savedGroupBuying = groupBuyingRepository.save(groupBuying);

        // 공구 참여 엔티티 생성(사용자 id, 공구 id, 역할-주최자, 신청 수량) → 공구 참여 레포지토리의 save() 실행 → 공구 참여 id 반환 시 성공
        GroupBuyingParticipation groupBuyingParticipation = GroupBuyingParticipation.builder()
                .member(member)
                .groupBuying(groupBuying)
                .role(UserRole.ORGANIZER)
                .applyQuantity(request.organizerQuantity())
                .paymentStatus(PaymentStatus.Complete)
                .build();

//        각각 id 반환 실패 시 예외 처리

        // Response DTO 반환
        return new GroupBuyingResponse.Create(
                savedGroupBuying.getId(), groupBuyingParticipation.getId());
    }
}
