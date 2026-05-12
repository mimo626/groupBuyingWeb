package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.entity.GroupBuying;
import com.example.groupbuyingweb.domain.entity.GroupBuyingImage;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Value("${file.upload.dir}")
    private String uploadDir;

    // 공구 개설
    @Transactional
    public GroupBuyingResponse.Create addGroupBuying(GroupBuyingRequest.Create request, List<MultipartFile> images, String memberId) {
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
                .deadline(request.deadline())
                .neighborhoodName(addressService.createNeighborhoodName(request.entX(), request.entY()))
                .build();

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 이미지 처리 및 GroupBuyingImage 엔티티 생성
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

                    // 나중에 HTML 화면에서 <img> 태그로 보여줄 때 사용할 URL 경로
                    String imageUrl = "/uploads/" + storedFilename;

                    File targetFile = new File(uploadDir + storedFilename);

                    try {
                        file.transferTo(targetFile); // 실제 폴더에 저장

                        // 첫 번째로 올라온 이미지를 썸네일(대표 이미지)로 지정!
                        boolean isThumbnail = (i == 0);

                        GroupBuyingImage imageEntity = GroupBuyingImage.builder()
                                .originalFilename(originalFilename)
                                .storedFilename(storedFilename)
                                .imageUrl(imageUrl)
                                .isThumbnail(isThumbnail)
                                .build();

                        // 공구 엔티티에 이미지 추가 (JPA가 나중에 알아서 DB에 INSERT 해줌)
                        groupBuying.addImage(imageEntity);

                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 중 오류 발생", e);
                    }
                }
            }
        }

        // 3. DB 저장 (Cascade 옵션 때문에 GroupBuying만 저장해도 Image까지 같이 INSERT 됩니다)
        groupBuyingRepository.save(groupBuying);

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
                member, groupBuying, UserRole.PARTICIPANT, applyQuantity, PaymentStatus.Incomplete
        );

        // 단가 계산 (총액 / 목표수량)
        double unitPrice = groupBuying.getTotalPrice() / targetQuantity;

        // 참여자의 포인트 결제 진행 (DB 조회 없이 만들어둔 객체를 그대로 넘김)
        pointService.payPoint(member, participation, unitPrice);

        // 상태 변경: 목표 수량 달성 시 공구 시작 및 채팅방 생성
        if (totalQuantityAfterApply == targetQuantity) {
            processStatusChange(groupBuyingId, GroupBuyingStatus.START, null, null);
        }

        return new GroupBuyingResponse.Participate(groupBuyingId, participation.getId());
    }

    // 공구 목록 조회(검색/필터링)
    public Page<GroupBuyingResponse.GroupBuyings> getGroupBuyings(GroupBuyingRequest.SearchCondition condition, Pageable pageable) {

        // Repository에서 Page<GroupBuying> 조회
        return groupBuyingRepository.searchGroupBuyings(condition.category(), condition.keyword(), pageable)
                // 메서드 참조(::) 대신 람다식(->)을 사용
                .map(groupBuying -> {
                    int currentQuantity = calculateCurrentQuantity(groupBuying.getId());

                    return GroupBuyingResponse.GroupBuyings.of(groupBuying, currentQuantity);
                });
    }

    // 공구 진행 상태 변경
    @Transactional
    public GroupBuyingResponse.UpdateStatus updateStatusFromRequest(Long groupBuyingId, GroupBuyingRequest.UpdateStatus request) {
        // 상태 변경 로직 호출 (DTO의 값들을 풀어서 전달)
        GroupBuying updatedGroupBuying = processStatusChange(
                groupBuyingId, request.status(), request.trackingNumber(), request.meetingAt()
        );

        // 엔티티를 응답 DTO로 변환하여 반환
        return GroupBuyingResponse.UpdateStatus.from(updatedGroupBuying);
    }
    // 서비스 내부 호출용 공구 진행상태 변경 메서드 (공구 참여 로직에서도 호출 가능)
    @Transactional
    public GroupBuying processStatusChange(Long groupBuyingId, GroupBuyingStatus newStatus, String trackingNumber, LocalDateTime meetingAt) {
        GroupBuying groupBuying = groupBuyingRepository.findById(groupBuyingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_GROUP_BUYING));

        groupBuying.updateStatus(newStatus);

        switch (newStatus) {
            case START -> {
                // 채팅방 생성
                chatRoomService.createChatRoom(groupBuying);
            }
            case PURCHASED -> {

            }
            case SHIPPING -> {
                if (trackingNumber != null) groupBuying.updateTrackingNumber(trackingNumber);
            }
            case MEETING_SCHEDULED -> {
                if (meetingAt != null) groupBuying.updateMeetingAt(meetingAt);
            }
            case SETTLING -> {
                //TODO 공구 참여자의 포인트 정산 상태 완료로 변경(공구 참여할 땐 미완료가 맞는지)
                pointService.settlePoint(groupBuyingId);
            }
            case CLOSED -> {
                //TODO 공구 종료
            }
            default -> {
                break;
            }
        }
        // TODO 상태 변경 시 어떤 내용을 전달할 지 정하기
        // 변경된 상태
//        chatRoomService.sendSystemMessage(newStatus.getDescription());

        if (newStatus != GroupBuyingStatus.START) {
            chatRoomService.sendSystemMessage(groupBuying.getId(), newStatus);
        }

        return groupBuying; // 변경된 엔티티 반환
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_GROUP_BUYING));
    }

    // 멤버 조회 (임시 로직 격리)
    private Member getMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_MEMBER)
        );
    }
}