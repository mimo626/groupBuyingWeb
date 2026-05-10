package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // @Autowired 대신 생성자 주입 (권장 방식)
@Transactional(readOnly = true)
public class GroupBuyingService {

    private final GroupBuyingRepository groupBuyingRepository;
    private final MemberRepository memberRepository;
    private final GroupBuyingParticipationRepository participationRepository; // 변수명 간소화
    private final PointService pointService;

    // 공구 개설
    @Transactional
    public GroupBuyingResponse.Create addGroupBuying(GroupBuyingRequest.Create request, String memberId) {
        Member member = getMember(memberId);

        GroupBuying groupBuying = GroupBuying.builder()
                .member(member)
                .title(request.title())
                .productName(request.productName())
                .category(request.category())
                .productContent(request.productContent())
                .totalPrice(request.totalPrice())
                .targetQuantity(request.targetQuantity())
                .entX(request.entX())
                .entY(request.entY())
                .meetingPlace(request.meetingPlace())
                .productUrl(request.productUrl())
                .productImageUrl(request.productImageUrl())
                .deadline(request.deadline())
                .neighborhoodName("상봉동")
                .build();

        GroupBuying savedGroupBuying = groupBuyingRepository.save(groupBuying);

        // 중복 분리: 공통 참여 엔티티 생성 메서드 호출 (주최자)
        GroupBuyingParticipation participation = createParticipation(
                member, savedGroupBuying, UserRole.ORGANIZER, request.organizerQuantity(), PaymentStatus.Complete
        );

        return new GroupBuyingResponse.Create(savedGroupBuying.getId(), participation.getId());
    }

    // 공구 참여 및 공구 시작
    @Transactional
    public GroupBuyingResponse.Participate participateGroupBuying(Integer applyQuantity, String memberId, Long groupBuyingId) {
        GroupBuying groupBuying = getGroupBuying(groupBuyingId);
        int targetQuantity = groupBuying.getTargetQuantity(); // 리포지토리 재조회 불필요 (엔티티 사용)
        int currentQuantity = calculateCurrentQuantity(groupBuyingId);

        int totalQuantityAfterApply = currentQuantity + applyQuantity;

        // 수량 초과 예외 처리
        if (totalQuantityAfterApply > targetQuantity) {
            throw new IllegalStateException("모집 수량을 초과하여 신청할 수 없습니다.");
        }

        Member member = getMember(memberId);

        // 참여자의 포인트 결제
        pointService.payPoint(memberId, groupBuyingId, groupBuying.getTotalPrice(), applyQuantity);

        // 공구 참여 엔티티 생성 메서드 호출 (참여자 기준)
        GroupBuyingParticipation participation = createParticipation(
                member, groupBuying, UserRole.PARTICIPANT, applyQuantity, PaymentStatus.Complete
        );

        // 상태 변경: 목표 수량 달성 시 공구 시작
        if (totalQuantityAfterApply == targetQuantity) {
            groupBuying.updateStatus(GroupBuyingStatus.START);
            // @Transactional 덕분에 groupBuyingRepository.save()를 호출하지 않아도 트랜잭션 종료 시 자동 UPDATE 쿼리 발생
        }

        return new GroupBuyingResponse.Participate(groupBuyingId, participation.getId());
    }

    /* ================= 공통 로직 ================= */

    // 공구 상세 Dto 생성
    public GroupBuyingResponse.Detail getGroupBuyingById(Long groupBuyingId) {
        GroupBuying groupBuying = getGroupBuying(groupBuyingId);
        int currentQuantity = calculateCurrentQuantity(groupBuyingId);
        return GroupBuyingResponse.Detail.of(groupBuying, currentQuantity);
    }

    // 공구 참여 엔티티 생성
    private GroupBuyingParticipation createParticipation(Member member, GroupBuying groupBuying, UserRole role, int applyQuantity, PaymentStatus paymentStatus) {
        GroupBuyingParticipation participation = GroupBuyingParticipation.builder()
                .member(member)
                .groupBuying(groupBuying)
                .role(role)
                .applyQuantity(applyQuantity)
                .paymentStatus(paymentStatus)
                .build();
        return participationRepository.save(participation);
    }

    // 공구의 현재 총 신청 수량 계산
    private int calculateCurrentQuantity(Long groupBuyingId) {
        return participationRepository.findAllByGroupBuyingId(groupBuyingId)
                .stream()
                .mapToInt(GroupBuyingParticipation::getApplyQuantity)
                .sum();
    }

    // 공구 엔티티 조회
    private GroupBuying getGroupBuying(Long groupBuyingId) {
        return groupBuyingRepository.findById(groupBuyingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공동구매 게시글입니다."));
    }

    // 멤버 조회 (임시 로직 격리)
    private Member getMember(String memberId) {
        // TODO: 나중에 findById(memberId) 로 변경
        return memberRepository.findAll().get(0);
    }
}