package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.dto.request.GroupBuyingRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingResponse;
import com.example.groupbuyingweb.domain.entity.h2.UserNearbyAddress;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingImage;
import com.example.groupbuyingweb.domain.entity.mysql.GroupBuyingParticipation;
import com.example.groupbuyingweb.domain.entity.mysql.Member;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.PaymentStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.h2.UserNearbyAddressRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingImageRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingRepository;
import com.example.groupbuyingweb.repository.mysql.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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
    private  final GroupBuyingImageRepository groupBuyingImageRepository;
    private  final UserNearbyAddressRepository userNearbyAddressRepository;

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
                .meetingAddress(request.meetingAddress())
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

        return new GroupBuyingResponse.Participate(groupBuyingId, participation.getId(), applyQuantity);
    }

    // 공구 목록 조회(검색/필터링)
    public Page<GroupBuyingResponse.GroupBuyings> getGroupBuyings(GroupBuyingRequest.SearchCondition condition, Pageable pageable, String loggedInUserId) {

        List<UserNearbyAddress> addressEntities = userNearbyAddressRepository.findAllByMemberId(loggedInUserId);

        List<String> userNearbyAddressList = addressEntities.stream()
                .map(UserNearbyAddress::getNeighborhoodName)
                .toList();

        // 방어 로직: 등록된 동네가 하나도 없다면 DB를 조회할 필요 없이 빈 페이지 반환
        if (userNearbyAddressList.isEmpty()) {
            return Page.empty(pageable);
        }

        // Repository에서 Page<GroupBuying> 조회
        return groupBuyingRepository.searchGroupBuyings(
                LocalDateTime.now(),
                condition.category(),
                condition.keyword(),
                userNearbyAddressList, // 추가된 파라미터 전달
                pageable
            ).map(groupBuying -> {
                    int currentQuantity = calculateCurrentQuantity(groupBuying.getId());

                    // map을 통해 엔티티 객체에서 URL만 추출
                    String thumbnailUrl = groupBuyingImageRepository
                            .findByGroupBuyingIdAndIsThumbnailTrue(groupBuying.getId())
                            .map(GroupBuyingImage::getImageUrl)
                            .orElse(null);

                    // D-Day 계산
                    String dDayString = calculateDday(groupBuying);

                    // DTO 변환 시 thumbnailUrl 추가
                    return GroupBuyingResponse.GroupBuyings.of(groupBuying, thumbnailUrl, currentQuantity, dDayString);
                });
    }
    // 공구 진행 상태 변경
    @Transactional
    public GroupBuyingResponse.UpdateStatus updateStatusFromRequest(Long groupBuyingId, GroupBuyingRequest.@NonNull UpdateStatus request) {
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

        // 변경 전 상태 기록
        GroupBuyingStatus previousStatus = groupBuying.getStatus();

        groupBuying.updateStatus(newStatus);

        // 메모리가 초기화되기 전에 DB에 즉시 UPDATE 쿼리를 날려버림
        groupBuyingRepository.saveAndFlush(groupBuying);

        System.out.println("processStatusChange: " + groupBuying.getStatus().toString());
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
                //pointService.settlePoint(groupBuyingId);
            }
            case CLOSED -> {
                // 1. 영속성 컨텍스트가 날아가기 전에 주최자 ID를 미리 확보합니다.
                // (프록시 객체라도 식별자(ID) 조회는 예외를 발생시키지 않습니다)
                String organizerId = groupBuying.getMember().getId();

                // 2. 공구 주최자, 참여자의 경험치 증가
                // (이 과정에서 @Modifying(clearAutomatically = true)로 인해 세션이 초기화될 수 있음)
                participationRepository.findAllByGroupBuyingId(groupBuyingId)
                        .forEach(g -> {
                            String memberId = g.getMember().getId();
                            UserRole role = g.getRole();

                            int expToAppend = (role == UserRole.ORGANIZER) ? 2 : 1;
                            memberRepository.incrementAcornExp(memberId, expToAppend);
                        });

                // 3. 최신 경험치를 반영하기 위해 DB에서 주최자 정보를 다시 조회합니다.
                Member organizer = memberRepository.findById(organizerId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_MEMBER)); // 적절한 예외 처리로 변경해주세요

                // 4. 주최자 보상 지급 (최신 경험치 기준)
                Double acornLevel = (double) Math.min(20, (organizer.getAcornExp() / 10) + 1);
                pointService.chargePoint(organizerId, acornLevel * 100);
                System.out.println("CLOSED: " + groupBuying.getStatus().toString());

            }
            default -> {
                break;
            }
        }

        // 모집 중 -> 공구 종료로 직행한 경우(채팅방 없음) 시스템 메시지 전송 생략
        if (previousStatus == GroupBuyingStatus.RECRUITING && newStatus == GroupBuyingStatus.CLOSED) {
            System.out.println("모집 기간 만료 마감. 채팅방이 없어 시스템 메시지를 생략합니다.");
        } else {
            chatRoomService.sendSystemMessage(groupBuying.getId(), newStatus);
        }
        return groupBuying; // 변경된 엔티티 반환
    }

    @Transactional
    public String getMemberAddress(String memberId) {
        Member member = getMember(memberId);
        return member.getAddress();
    }

    // 공구 상세 수정
    @Transactional
    public GroupBuyingResponse.Detail updateGroupBuying(
            Long groupBuyingId,
            GroupBuyingRequest.Create request, // 수정용 Request DTO (Create와 비슷하게 생겼을 겁니다)
            List<MultipartFile> newImages,
            String loggedInUserId) {

        // 1. 기존 공구 엔티티 조회 (없으면 예외 발생)
        GroupBuying groupBuying = getGroupBuying(groupBuyingId);

        // 2. 권한 체크 (주최자만 수정 가능)
        if (!groupBuying.getMember().getId().equals(loggedInUserId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다."); // 예외는 프로젝트 정책에 맞게 변경하세요
        }

        // 3. 주소가 변경되었을 수 있으므로 동네 이름 다시 계산
        String updatedNeighborhoodName = addressService.createNeighborhoodName(request.entX(), request.entY());

        // 4. 공구 기본 정보 업데이트 (JPA 더티 체킹 활용)
        // ※ GroupBuying 엔티티 내부에 update() 같은 비즈니스 메서드를 만들어두는 것이 좋습니다. (아래 참고)
        // TODO 주최자 수량 공구참여에서 수정해야함
        // TODO 이미지 변경 반영안됨
        groupBuying.update(
                request.title(),
                request.productName(),
                request.category(),
                request.productContent(),
                request.totalPrice(),
                request.targetQuantity(),
                request.entX(),
                request.entY(),
                request.meetingPlace(),
                request.meetingAddress(),
                request.productUrl(),
                request.deadline(),
                updatedNeighborhoodName
        );

        // 5. 이미지 수정 로직 (새로운 이미지가 업로드된 경우에만 실행)
        if (newImages != null && !newImages.isEmpty() && !newImages.get(0).isEmpty()) {

            // (선택사항) 서버 디스크에 저장된 기존 실제 파일들을 삭제하는 로직을 여기에 추가하면 용량 관리에 좋습니다.

            // 기존 DB 이미지 정보 초기화 (고아 객체 제거 orphanRemoval = true 설정 필요)
            groupBuying.clearImages();

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 새로운 이미지 저장 로직 (Create 로직과 동일)
            for (int i = 0; i < newImages.size(); i++) {
                MultipartFile file = newImages.get(i);

                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                    String imageUrl = "/uploads/" + storedFilename;
                    File targetFile = new File(uploadDir + storedFilename);

                    try {
                        file.transferTo(targetFile);

                        boolean isThumbnail = (i == 0);

                        GroupBuyingImage imageEntity = GroupBuyingImage.builder()
                                .originalFilename(originalFilename)
                                .storedFilename(storedFilename)
                                .imageUrl(imageUrl)
                                .isThumbnail(isThumbnail)
                                .build();

                        groupBuying.addImage(imageEntity);

                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 중 오류 발생", e);
                    }
                }
            }
        }

        // 현재 수량 및 기타 권한을 다시 계산하여 DTO로 반환
        int currentQuantity = calculateCurrentQuantity(groupBuyingId);
        boolean hasParticipants = participationRepository.existsByGroupBuyingIdAndRole(groupBuyingId, UserRole.PARTICIPANT);

        // 수정을 한 사람은 주최자이므로 isOrganizer=true, isParticipant=false 로 세팅
        return GroupBuyingResponse.Detail.of(groupBuying, currentQuantity, true, false, hasParticipants);
    }

    /* ================= 공통 로직 ================= */

    // 공구 상세 Dto 생성
    @Transactional
    public GroupBuyingResponse.Detail getGroupBuyingById(Long groupBuyingId, String loggedInUserId) {
        // 조회수 증가
        groupBuyingRepository.incrementViewCount(groupBuyingId);

        // 공동구매 엔티티 조회 (예외 처리 생략)
        GroupBuying groupBuying = getGroupBuying(groupBuyingId);

        // 현재 모집된 수량
        int currentQuantity = calculateCurrentQuantity(groupBuyingId);

        // 권한 및 상태 체크 로직
        boolean isOrganizer = false;
        boolean isParticipant = false;

        // 주최자를 제외한 참여자가 존재하는지 DB에서 확인
        boolean hasParticipants = participationRepository
                .existsByGroupBuyingIdAndRole(groupBuyingId, UserRole.PARTICIPANT);

        if (loggedInUserId != null) {
            // 주최자 여부 확인: 게시글 작성자 ID와 로그인 유저 ID 비교
            isOrganizer = groupBuying.getMember().getId().equals(loggedInUserId);

            // 주최자가 아니라면, 참여자 엔티티에서 현재 유저가 있는지 확인
            if (!isOrganizer) {
                isParticipant = participationRepository
                        .existsByGroupBuyingIdAndMemberIdAndRole(groupBuyingId, loggedInUserId, UserRole.PARTICIPANT);
            }
        }
        return GroupBuyingResponse.Detail.of(groupBuying, currentQuantity, isOrganizer, isParticipant, hasParticipants);
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

    // 디데이 계산
    private String calculateDday(GroupBuying groupBuying) {
        LocalDateTime deadline = groupBuying.getDeadline();
        LocalDateTime now = LocalDateTime.now();

        // 1. 이미 마감 기한이 지난 경우
        if (now.isAfter(deadline)) {
            return "마감";
        }

        Duration duration = Duration.between(now, deadline);
        long hours = duration.toHours();

        // 2. 하루(24시간) 미만으로 남은 경우
        if (hours < 24) {
            if (hours == 0) {
                long minutes = duration.toMinutes();
                return minutes + "분 남음";
            }
            return hours + "시간 남음";
        }

        // 3. 하루(24시간) 이상 남은 경우
        long days = duration.toDays();
        return "D-" + days;
    }
}