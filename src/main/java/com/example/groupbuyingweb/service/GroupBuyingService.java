package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // @Autowired 대신 생성자 주입 (권장 방식)
@Transactional(readOnly = true)
public class GroupBuyingService {

    private final GroupBuyingRepository groupBuyingRepository;
    private final MemberRepository memberRepository;
    private final GroupBuyingParticipationRepository participationRepository;
    private final PointService pointService;
    private final AddressService addressService;
    private final ChatRoomService chatRoomService;

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
                .neighborhoodName(addressService.createNeighborhoodName(request.entX(), request.entY()))
                .build();

        GroupBuying savedGroupBuying = groupBuyingRepository.save(groupBuying);

        // 공구 참여 엔티티 생성 (주최자)
        GroupBuyingParticipation participation = createParticipation(
                member, savedGroupBuying, UserRole.ORGANIZER, request.organizerQuantity(), PaymentStatus.Complete
        );

        // 단가 계산 (총액 / 목표수량)
        double unitPrice = groupBuying.getTotalPrice() / groupBuying.getTargetQuantity();

        // 주최자의 포인트 결제 진행
        pointService.payPoint(member, participation, unitPrice);

        return new GroupBuyingResponse.Create(savedGroupBuying.getId(), participation.getId());
    }

    // 공구 참여 및 공구 시작
    @Transactional
    public GroupBuyingResponse.Participate participateGroupBuying(Integer applyQuantity, String memberId, Long groupBuyingId) {
        GroupBuying groupBuying = getGroupBuying(groupBuyingId);
        int targetQuantity = groupBuying.getTargetQuantity();
        int currentQuantity = calculateCurrentQuantity(groupBuyingId);

        int totalQuantityAfterApply = currentQuantity + applyQuantity;

        // 수량 초과 예외 처리
        if (totalQuantityAfterApply > targetQuantity) {
            throw new BusinessException(ErrorCode.EXCEED_TARGET_QUANTITY);
        }

        Member member = getMember(memberId);

        // 공구 참여 엔티티 생성
        GroupBuyingParticipation participation = createParticipation(
                member, groupBuying, UserRole.PARTICIPANT, applyQuantity, PaymentStatus.Complete
        );

        // 단가 계산 (총액 / 목표수량)
        double unitPrice = groupBuying.getTotalPrice() / targetQuantity;

        // 참여자의 포인트 결제 진행 (DB 조회 없이 만들어둔 객체를 그대로 넘김)
        pointService.payPoint(member, participation, unitPrice);

        // 상태 변경: 목표 수량 달성 시 공구 시작
        if (totalQuantityAfterApply == targetQuantity) {
            processStatusChange(groupBuyingId, GroupBuyingStatus.START, null, null);
            // TODO 채팅방 생성
        }

        return new GroupBuyingResponse.Participate(groupBuyingId, participation.getId());
    }

    // 공구 목록 조회(검색/필터링)
    public Page<GroupBuyingResponse.List> getGroupBuyings(GroupBuyingRequest.SearchCondition condition, Pageable pageable) {

        // Repository에서 Page<GroupBuying> 조회
        return groupBuyingRepository.searchGroupBuyings(condition.category(), condition.keyword(), pageable)
                // 메서드 참조(::) 대신 람다식(->)을 사용
                .map(groupBuying -> {
                    int currentQuantity = calculateCurrentQuantity(groupBuying.getId());

                    return GroupBuyingResponse.List.of(groupBuying, currentQuantity);
                });
    }

    // 공구 진행 상태 변경
    @Transactional
    public void updateStatusFromRequest(Long id, String memberId, GroupBuyingRequest.UpdateStatus request) {
        // 권한 체크 로직

        // 상태 변경 로직 호출 (DTO의 값들을 풀어서 전달)
        processStatusChange(id, request.status(), request.trackingNumber(), request.meetingAt());
    }

    // 서비스 내부 호출용 공구 진행상태 변경 메서드 (공구 참여 로직에서도 호출 가능)
    @Transactional
    public void processStatusChange(Long id, GroupBuyingStatus newStatus, String trackingNumber, LocalDateTime meetingAt) {
        GroupBuying groupBuying = groupBuyingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공구입니다."));

        // 엔티티 상태 업데이트
        groupBuying.updateStatus(newStatus);

        // 상태별 추가 비즈니스 로직 처리 (Java 14+ switch 표현식 활용)
        switch (newStatus) {
            case START -> {
                // TODO: 채팅방 생성 로직
            }
            case SHIPPING -> {
                if (trackingNumber != null) {
//                    groupBuying.setTrackingNumber(trackingNumber);
                }
            }
            case MEETING_SCHEDULED -> {
                if (meetingAt != null) {
//                    groupBuying.setMeetingTime(meetingAt);
                }
            }
            // RECRUITING, PURCHASED, SETTLING, CLOSED 등은 엔티티 상태만 변경된다면 생략 가능
            default -> {}
        }

        // 채팅방에 시스템 메시지 전송 (알림)
//        chatRoomService.sendSystemMessage(groupBuying.getId(), newStatus.getDescription());
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CREATE_GROUP_BUYING));
    }

    // 멤버 조회 (임시 로직 격리)
    private Member getMember(String memberId) {
        // TODO: 나중에 findById(memberId) 로 변경
        return memberRepository.findAll().get(0);
    }
}